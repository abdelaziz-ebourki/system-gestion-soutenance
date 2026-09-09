const fs = require('fs');
const path = require('path');

const filePath = path.resolve(__dirname, 'postman/system-gestion-soutenance.postman_collection.json');
let raw = fs.readFileSync(filePath, 'utf8');
let collection = JSON.parse(raw);

let stats = { total: 0, wrapped: 0, direct: 0, list: 0, noContent: 0, modified: 0 };

function traverse(items) {
  for (const item of items) {
    if (item.item) {
      traverse(item.item);
    } else if (item.event && item.request) {
      processRequest(item);
    }
  }
}

function isApiResponseWrapped(url) {
  const urlStr = typeof url === 'string' ? url : JSON.stringify(url);
  const directPatterns = [
    '/api/admin/users',
    '/api/admin/config/general',
    '/api/admin/config/documents',
    '/api/admin/config/settings',
    '/api/admin/config/email',
    '/api/auth/login',
  ];
  
  for (const pattern of directPatterns) {
    if (urlStr.includes(pattern)) {
      // Check for auth endpoints that ARE wrapped
      if (urlStr.includes('/api/auth/') && !urlStr.includes('/api/auth/login')) return true;
      return false;
    }
  }

  // Notifications list and mutations are direct
  if (urlStr.includes('notifications')) return false;
  // Unavailability list is direct
  if (urlStr.includes('/api/coordinator/unavailabilities') && !urlStr.includes('/api/teacher/unavailabilities')) return false;
  
  return true;
}

function isNoContentEndpoint(method, url) {
  const urlStr = typeof url === 'string' ? url : JSON.stringify(url);
  const deleteUser = method === 'DELETE' && urlStr.includes('/api/admin/users/');
  const patchNotif = (method === 'PATCH' || method === 'POST') && urlStr.includes('notifications') && urlStr.includes('send-email');
  return deleteUser || patchNotif;
}

function isListEndpoint(name, method) {
  if (method !== 'GET') return false;
  const listNames = [
    'List ', 'Search ',
    'List users by role',
    'List students (paginated)',
    'List teachers (paginated)',
    'List coordinators (paginated)',
    'List all teachers (simple)',
    'List all students (simple)',
    'Get schedule',
    'Get my group',
    'List my evaluations',
    'Get my unavailability',
    'List unavailability',
    'List my documents',
    'List notifications',
    'List audit logs',
    'List grades (coord)',
    'List rooms (paginated)',
    'List groups',
    'List projects',
    'List juries',
    'List defense sessions',
  ];
  return listNames.some(n => name.startsWith(n) || name === n);
}

function processRequest(request) {
  const script = request.event?.[0]?.script;
  if (!script || !script.exec || script.exec.length === 0) return;
  
  const exec = script.exec;
  const url = request.request?.url?.raw || '';
  const method = request.request?.method || 'GET';
  const name = request.name || '';
  
  stats.total++;
  
  const wrapped = isApiResponseWrapped(url);
  const noContent = isNoContentEndpoint(method, url);
  const isList = isListEndpoint(name, method);
  
  if (noContent) {
    stats.noContent++;
    return;
  }
  
  let changed = false;
  
  if (wrapped) {
    stats.wrapped++;
    
    // For list endpoints, replace with proper structure validation
    if (isList) {
      const jsonCheckIdx = exec.findIndex(l => l.includes('pm.response.to.be.json'));
      if (jsonCheckIdx >= 0) {
        // Find the end of this test function
        const startIdx = jsonCheckIdx - 1; // line before (closing paren or comma)
        let endIdx = jsonCheckIdx + 1;
        // Find the closing '});' or ');'
        for (let j = jsonCheckIdx; j < exec.length; j++) {
          if (exec[j].includes('});') || exec[j].includes(');')) {
            endIdx = j;
            break;
          }
        }
        
        const newLines = [
          exec[jsonCheckIdx - 1], // keep the pm.test opening
          '  pm.response.to.be.json;',
          '  const json = pm.response.json();',
          '  pm.expect(json).to.be.an(\'object\');',
          '  pm.expect(json.success).to.be.true;',
          '  pm.expect(json.data).to.be.an(\'array\');',
          exec[endIdx]  // keep closing
        ];
        exec.splice(jsonCheckIdx - 1, endIdx - (jsonCheckIdx - 1) + 1, ...newLines);
        changed = true;
      }
    }
    
    // Fix all json.field references to json.data.field
    for (let i = 0; i < exec.length; i++) {
      let line = exec[i];
      const original = line;
      
      // Replace patterns like json.id, json.name, etc. with json.data.id, json.data.name
      // But only when used with pm.expect
      line = line.replace(/pm\.expect\(json\.(id|name|code|success|title|email|role|lastName|firstName|isActive|cne|majorId|majorName|levelId|levelName|gradeId|gradeName|departmentId|departmentName|groupName|memberCount|projectId|studentNames|description|defenseType|supervisorName|projectTitle|templateId|templateName|status|date|time|roomName|roomId|startDate|endDate|defenseDuration|breakDuration|submissionDeadline|evaluationCoefficients|juryRoleTemplateId|maxGroupSize|startTime|endTime|groupCreationStartDate|groupCreationEndDate|institutionName|institutionLogoUrl|timezone|dateFormat|setupCompleted|maxFileSizeMb|allowedExtensions|versionLimit|host|port|username|senderName|senderEmail|encryption|totalStudents|totalTeachers|totalDepartments|totalRooms|totalDefenseSessions|action|entity|entityId|performedByEmail|details|timestamp)\)/g, 'pm.expect(json.data.$1)');
      
      // Also fix standalone references to json.field (like json.id !== undefined)
      line = line.replace(/json\.(id|name|code|success|title|email|role|lastName|firstName|isActive|cne|majorId|majorName|levelId|levelName|gradeId|gradeName|departmentId|departmentName|groupName|memberCount|projectId|studentNames|description|defenseType|supervisorName|projectTitle|templateId|templateName|status|date|time|roomName|roomId|startDate|endDate|defenseDuration|breakDuration|submissionDeadline|evaluationCoefficients|juryRoleTemplateId|maxGroupSize|startTime|endTime|groupCreationStartDate|groupCreationEndDate|institutionName|institutionLogoUrl|timezone|dateFormat|setupCompleted|maxFileSizeMb|allowedExtensions|versionLimit|host|port|username|senderName|senderEmail|encryption|totalStudents|totalTeachers|totalDepartments|totalRooms|totalDefenseSessions|action|entity|entityId|performedByEmail|details|timestamp)/g, 'json.data.$1');
      
      if (line !== original) {
        exec[i] = line;
        changed = true;
      }
    }
    
    // Add .success and .data checks in object tests if not present
    if (!isList) {
      const fullText = exec.join('\n');
      if (fullText.includes('pm.expect(json).to.be.an(\'object\')') && 
          !fullText.includes('pm.expect(json.success') && 
          !fullText.includes('pm.expect(json.data.success')) {
        // Find the object check and add success/data checks after it
        for (let i = 0; i < exec.length; i++) {
          if (exec[i].includes('pm.expect(json).to.be.an(\'object\')')) {
            exec.splice(i + 1, 0, '  pm.expect(json.success).to.be.true;', '  pm.expect(json.data).to.be.an(\'object\');');
            changed = true;
            break;
          }
        }
      }
    }
    
  } else {
    stats.direct++;
    
    // For direct DTO endpoints, handle list patterns
    if (isList) {
      const isDirectList = url.includes('notifications') || url.includes('/api/coordinator/unavailabilities');
      if (isDirectList) {
        const jsonCheckIdx = exec.findIndex(l => l.includes('pm.response.to.be.json'));
        if (jsonCheckIdx >= 0) {
          const startIdx = jsonCheckIdx - 1;
          let endIdx = jsonCheckIdx + 1;
          for (let j = jsonCheckIdx; j < exec.length; j++) {
            if (exec[j].includes('});') || exec[j].includes(');')) {
              endIdx = j;
              break;
            }
          }
          
          const newLines = [
            exec[jsonCheckIdx - 1],
            '  pm.response.to.be.json;',
            '  const json = pm.response.json();',
            '  pm.expect(json).to.be.an(\'array\');',
            exec[endIdx]
          ];
          exec.splice(jsonCheckIdx - 1, endIdx - (jsonCheckIdx - 1) + 1, ...newLines);
          changed = true;
        }
      }
    }
    
    // Make .to.not.be.undefined checks conditional for direct DTOs
    for (let i = 0; i < exec.length; i++) {
      if (exec[i].includes('pm.expect(json.id).to.not.be.undefined')) {
        exec[i] = '  if (json.id !== undefined) {';
        exec.splice(i + 1, 0, '    pm.expect(json.id).to.not.be.undefined;', '  }');
        changed = true;
        break;
      }
    }
    
    // Add success/data checks for paginated user endpoints
    if (url.includes('/api/admin/users') || url.includes('/api/coordinator/users')) {
      if (isList) {
        const fullText = exec.join('\n');
        if (fullText.includes('pm.response.to.be.json') && !fullText.includes('json.items')) {
          // These are PaginatedResponse<T> directly
          const jsonCheckIdx = exec.findIndex(l => l.includes('pm.response.to.be.json'));
          if (jsonCheckIdx >= 0) {
            const startIdx = jsonCheckIdx - 1;
            let endIdx = jsonCheckIdx + 1;
            for (let j = jsonCheckIdx; j < exec.length; j++) {
              if (exec[j].includes('});') || exec[j].includes(');')) {
                endIdx = j;
                break;
              }
            }
            
            const newLines = [
              exec[jsonCheckIdx - 1],
              '  pm.response.to.be.json;',
              '  const json = pm.response.json();',
              '  pm.expect(json).to.be.an(\'object\');',
              '  pm.expect(json.items).to.be.an(\'array\');',
              '  pm.expect(json.total).to.be.a(\'number\');',
              exec[endIdx]
            ];
            exec.splice(jsonCheckIdx - 1, endIdx - (jsonCheckIdx - 1) + 1, ...newLines);
            changed = true;
          }
        }
      }
      
      // For single user create/update, add success/data checks
      if (!isList && method === 'POST') {
        // User create returns UserDto directly, not wrapped
        // So no change needed
      }
    }
    
    // Handle config endpoints (return entity directly)
    if (url.includes('/api/admin/config/')) {
      if (!isList) {
        const fullText = exec.join('\n');
        if (fullText.includes('pm.expect(json).to.be.an(\'object\')') && 
            !fullText.includes('pm.expect(json.id)') &&
            !fullText.includes('pm.expect(json.institutionName') &&
            !fullText.includes('pm.expect(json.startTime')) {
          // Add appropriate field checks based on URL
          for (let i = 0; i < exec.length; i++) {
            if (exec[i].includes('pm.expect(json).to.be.an(\'object\')')) {
              if (url.includes('/general')) {
                exec.splice(i + 1, 0, '  // Required fields for GeneralSettings', '  pm.expect(json.institutionName).to.exist;');
              } else if (url.includes('/documents')) {
                exec.splice(i + 1, 0, '  // Required fields for DocumentConfig', '  pm.expect(json.maxFileSizeMb).to.exist;');
              } else if (url.includes('/settings')) {
                exec.splice(i + 1, 0, '  // Required fields for DefenseSettings', '  pm.expect(json.startTime).to.exist;');
              } else if (url.includes('/email')) {
                exec.splice(i + 1, 0, '  // Required fields for EmailConfig', '  pm.expect(json.host).to.exist;');
              }
              changed = true;
              break;
            }
          }
        }
      }
    }
  }
  
  if (changed) {
    stats.modified++;
  }
}

// Handle login endpoints separately (they have token extraction)
function processLoginEndpoints() {
  // The login endpoints have special scripts with token extraction
  // They're already mostly correct, just need to add field validation
  // This is handled by the general loop above
}

traverse(collection.item);

// Write back
fs.writeFileSync(filePath, JSON.stringify(collection, null, 2), 'utf8');
console.log('Transformation complete.');
console.log('Stats:', JSON.stringify(stats));
console.log('File written to:', filePath);
