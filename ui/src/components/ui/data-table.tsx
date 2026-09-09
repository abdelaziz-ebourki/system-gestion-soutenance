import * as React from "react";
import {
  type ColumnDef,
  flexRender,
  getCoreRowModel,
  useReactTable,
  getPaginationRowModel,
  type SortingState,
  getSortedRowModel,
  type ColumnFiltersState,
  getFilteredRowModel,
  type PaginationState,
  type Table as TanStackTable,
  type Row,
} from "@tanstack/react-table";
import { DEFAULT_API_LIMIT, PAGE_SIZE_OPTIONS } from "@/lib/constants";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { DropdownMenu, DropdownMenuCheckboxItem, DropdownMenuContent, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { RotateCcw, ArrowUpDown, ArrowUp, ArrowDown, Columns } from "lucide-react";
import { cn } from "@/lib/utils";

interface DataTableContextValue {
  table: TanStackTable<unknown>;
  labels: Required<DataTableLabels>;
  loading?: boolean;
  enableRowSelection?: boolean;
  onRowClick?: (row: unknown) => void;
  emptyMessage?: string;
  mergedColumns: ColumnDef<unknown, unknown>[];
  globalFilter: string;
  setGlobalFilter: (value: string) => void;
  searchCols: string[] | undefined;
  filters: DataTableFilter[] | undefined;
  columnVisibility: boolean | undefined;
  filterPlaceholder: string;
  hasActiveFilters: boolean;
  pageSizeOptions: number[];
}

const DataTableCtx = React.createContext<DataTableContextValue | null>(null);

function useDataTable<TData>() {
  const ctx = React.useContext(DataTableCtx) as DataTableContextValue & { table: TanStackTable<TData>; onRowClick?: (row: TData) => void; mergedColumns: ColumnDef<TData, unknown>[] } | null;
  if (!ctx) throw new Error("useDataTable must be used within DataTableProvider");
  return ctx;
}

interface DataTableFilter {
  column: string;
  label: string;
  options: { value: string; label: string }[];
}

interface DataTableLabels {
  pageXofY?: (page: number, total: number) => string;
  itemsPerPage?: string;
  previous?: string;
  next?: string;
  clearFilters?: string;
  allItems?: (label: string) => string;
  noResults?: string;
  columnsToggle?: string;
}

interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  loading?: boolean;
  error?: string | null;
  emptyMessage?: string;
  filterColumns?: string | string[];
  filterPlaceholder?: string;
  filters?: DataTableFilter[];
  manualPagination?: boolean;
  pageSize?: number;
  pageCount?: number;
  pagination?: PaginationState;
  onPaginationChange?: React.Dispatch<React.SetStateAction<PaginationState>>;
  onFiltering?: (active: boolean) => void;
  onRowClick?: (row: TData) => void;
  getRowId?: (row: TData) => string | number;
  pageSizeOptions?: number[];
  columnVisibility?: boolean;
  enableRowSelection?: boolean;
  onSelectedRowsChange?: (rows: TData[]) => void;
  labels?: DataTableLabels;
}

function getPageNumbers(current: number, total: number): (number | "...")[] {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);

  const pages: (number | "...")[] = [1];
  const start = Math.max(2, current - 2);
  const end = Math.min(total - 1, current + 2);

  if (start > 2) pages.push("...");
  for (let i = start; i <= end; i++) pages.push(i);
  if (end < total - 1) pages.push("...");
  pages.push(total);

  return pages;
}

const defaultLabels: Required<DataTableLabels> = {
  pageXofY: (page, total) => `Page ${page} sur ${total}`,
  itemsPerPage: "/ page",
  previous: "Précédent",
  next: "Suivant",
  clearFilters: "Effacer les filtres",
  allItems: (label) => `Tous les ${label.toLowerCase()}`,
  noResults: "Aucun résultat.",
  columnsToggle: "Colonnes",
};

function DataTableProvider<TData, TValue>({
  children,
  columns,
  data,
  loading,
  error,
  emptyMessage,
  filterColumns,
  filterPlaceholder = "Rechercher...",
  filters,
  manualPagination = false,
  pageSize = DEFAULT_API_LIMIT,
  pageCount,
  pagination,
  onPaginationChange,
  onFiltering,
  onRowClick,
  getRowId,
  pageSizeOptions = PAGE_SIZE_OPTIONS,
  columnVisibility,
  enableRowSelection,
  onSelectedRowsChange,
  labels: labelsProp,
}: DataTableProps<TData, TValue> & { children?: React.ReactNode }) {
  const labels = React.useMemo<Required<DataTableLabels>>(
    () => ({ ...defaultLabels, ...labelsProp }),
    [labelsProp],
  );
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [globalFilter, setGlobalFilter] = React.useState("");
  const deferredGlobalFilter = React.useDeferredValue(globalFilter);

  const [internalPagination, setInternalPagination] =
    React.useState<PaginationState>({ pageIndex: 0, pageSize });
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({});

  const mergedColumns = React.useMemo(() => {
    if (!enableRowSelection) return columns;
    return [
      {
        id: "select",
        header: ({ table }: { table: TanStackTable<TData> }) => {
          const isAllSelected = table.getIsAllPageRowsSelected();
          const isSomeSelected = !isAllSelected && table.getIsSomePageRowsSelected();
          return (
            <Checkbox
              checked={isSomeSelected ? "indeterminate" : isAllSelected}
              onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
              aria-label="Select all"
            />
          );
        },
        cell: ({ row }: { row: Row<TData> }) => (
          <Checkbox
            checked={row.getIsSelected()}
            onCheckedChange={(value) => row.toggleSelected(!!value)}
            aria-label="Select row"
          />
        ),
        enableSorting: false,
        enableHiding: false,
      } as ColumnDef<TData, unknown>,
      ...columns,
    ];
  }, [columns, enableRowSelection]);

  const searchCols = React.useMemo(
    () => (typeof filterColumns === "string" ? [filterColumns] : filterColumns),
    [filterColumns],
  );

  const globalFilterFn = React.useCallback(
    (row: Row<TData>, _columnId: string, filterValue: string) => {
      if (!searchCols) return true;
      const query = String(filterValue).toLowerCase();
      return searchCols.some((colId) => {
        const val = row.getValue(colId);
        return String(val ?? "").toLowerCase().includes(query);
      });
    },
    [searchCols],
  );

   // eslint-disable-next-line react-hooks/incompatible-library
   const table = useReactTable({
    data,
    columns: mergedColumns,
     getRowId: getRowId ? (row) => String(getRowId(row)) : undefined,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: manualPagination ? undefined : getPaginationRowModel(),
    onSortingChange: setSorting,
    getSortedRowModel: getSortedRowModel(),
    onColumnFiltersChange: setColumnFilters,
    getFilteredRowModel: getFilteredRowModel(),
    manualPagination,
    pageCount,
    onPaginationChange: onPaginationChange ?? setInternalPagination,
    onGlobalFilterChange: setGlobalFilter,
    enableRowSelection: !!enableRowSelection,
    onRowSelectionChange: setRowSelection,
    globalFilterFn,
    state: { sorting, columnFilters, globalFilter: deferredGlobalFilter, pagination: pagination ?? internalPagination, rowSelection },
  });

  const hasActiveFilters = columnFilters.length > 0 || globalFilter.length > 0;

  const ctxValue = React.useMemo(() => ({
    table, labels, loading, enableRowSelection, onRowClick, emptyMessage, mergedColumns, globalFilter,
    setGlobalFilter, searchCols, filters, columnVisibility, filterPlaceholder, hasActiveFilters, pageSizeOptions,
  } as unknown as DataTableContextValue), [
    table, labels, loading, enableRowSelection, onRowClick, emptyMessage, mergedColumns, globalFilter,
    setGlobalFilter, searchCols, filters, columnVisibility, filterPlaceholder, hasActiveFilters, pageSizeOptions,
  ]);

  const prevFiltering = React.useRef(hasActiveFilters);
  React.useEffect(() => {
    if (prevFiltering.current !== hasActiveFilters) {
      prevFiltering.current = hasActiveFilters;
      onFiltering?.(hasActiveFilters);
    }
  }, [hasActiveFilters, onFiltering]);

  const onSelectionChangeRef = React.useRef(onSelectedRowsChange);
  React.useEffect(() => {
    onSelectionChangeRef.current = onSelectedRowsChange;
  });
  React.useEffect(() => {
    if (!onSelectionChangeRef.current) return;
    onSelectionChangeRef.current(table.getSelectedRowModel().rows.map((r) => r.original));
  }, [rowSelection, table]);

  if (error) return <div className="rounded-md border border-destructive/50 p-6 text-center text-sm text-destructive">{error}</div>;

  return (
    <DataTableCtx.Provider value={ctxValue}>
      <div>{children}</div>
    </DataTableCtx.Provider>
  );
}

function DataTableToolbar() {
  const { table, labels, searchCols, filters, columnVisibility, filterPlaceholder, hasActiveFilters, globalFilter, setGlobalFilter } = useDataTable();

  if (!searchCols && !filters?.length && !columnVisibility) return null;

  return (
    <div className="flex flex-wrap items-center gap-4 py-4">
      {searchCols && (
        <Input
          placeholder={filterPlaceholder}
          value={globalFilter ?? ""}
          onChange={(event) => {
            setGlobalFilter(event.target.value);
            table.setPageIndex(0);
          }}
          className="max-w-sm"
          data-testid="data-table-search"
        />
      )}
      {filters?.map((f) => {
        const column = table.getColumn(f.column);
        if (!column) return null;
        const ALL_VALUE = "__all__";
        const currentValue = (column.getFilterValue() as string) ?? ALL_VALUE;
        return (
          <Select
            key={f.column}
            value={currentValue}
            onValueChange={(v) => {
              column.setFilterValue(v === ALL_VALUE ? undefined : v);
              table.setPageIndex(0);
            }}
          >
            <SelectTrigger className="w-44">
              <span className="flex-1 text-left truncate">
                {currentValue && currentValue !== ALL_VALUE
                  ? f.options.find((o) => o.value === currentValue)?.label ?? currentValue
                  : f.label}
              </span>
            </SelectTrigger>
            <SelectContent position="popper">
              <SelectItem value={ALL_VALUE}>
                {labels.allItems(f.label)}
              </SelectItem>
              {f.options.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        );
      })}
      {columnVisibility && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm">
              <Columns className="mr-1 size-3" />
              {labels.columnsToggle}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {table.getAllLeafColumns().filter((col) => col.getCanHide()).map((col) => {
              const label = typeof col.columnDef.header === "string" ? col.columnDef.header : col.id;
              return (
                <DropdownMenuCheckboxItem
                  key={col.id}
                  checked={col.getIsVisible()}
                  onCheckedChange={col.getToggleVisibilityHandler()}
                >
                  {label}
                </DropdownMenuCheckboxItem>
              );
            })}
          </DropdownMenuContent>
        </DropdownMenu>
      )}
      {hasActiveFilters && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => {
            setGlobalFilter("");
            table.resetColumnFilters();
            table.setPageIndex(0);
          }}
        >
          <RotateCcw className="mr-1 size-3" />
          {labels.clearFilters}
        </Button>
      )}
    </div>
  );
}

function DataTableHeader() {
  const { table } = useDataTable();
  return (
    <TableHeader className="bg-muted/30">
      {table.getHeaderGroups().map((headerGroup) => (
        <TableRow key={headerGroup.id}>
          {headerGroup.headers.map((header) => (
            <TableHead
              key={header.id}
              onClick={header.column.getToggleSortingHandler()}
              aria-sort={
                header.column.getIsSorted() === "asc"
                  ? "ascending"
                  : header.column.getIsSorted() === "desc"
                    ? "descending"
                    : undefined
              }
              className={cn(
                "font-heading text-sm uppercase tracking-wider",
                header.column.getCanSort() ? "cursor-pointer select-none" : ""
              )}
            >
              {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
              {header.column.getCanSort() && !header.column.getIsSorted() ? (
                <ArrowUpDown className="ml-1 inline size-3 opacity-40" />
              ) : null}
              {header.column.getIsSorted() === "asc" ? (
                <ArrowUp className="ml-1 inline size-3" />
              ) : null}
              {header.column.getIsSorted() === "desc" ? (
                <ArrowDown className="ml-1 inline size-3" />
              ) : null}
            </TableHead>
          ))}
        </TableRow>
      ))}
    </TableHeader>
  );
}

function DataTableBody() {
  const { table, labels, onRowClick, emptyMessage, mergedColumns, loading } = useDataTable();

  if (loading) {
    return (
      <TableBody>
        {Array.from({ length: table.getState().pagination.pageSize || 10 }).map((_, i) => (
          <TableRow key={`skeleton-${i}`}>
            {mergedColumns.map((_, j) => (
              <TableCell key={`skeleton-cell-${j}`}>
                <Skeleton className="h-6 w-full" />
              </TableCell>
            ))}
          </TableRow>
        ))}
      </TableBody>
    );
  }

  return (
    <TableBody>
      {table.getRowModel().rows?.length ? (
        table.getRowModel().rows.map((row) => (
          <TableRow
            key={row.id}
            onClick={() => onRowClick?.(row.original)}
            className={cn(
              "even:bg-muted/10 transition-colors hover:bg-muted/40",
              onRowClick ? "cursor-pointer" : ""
            )}
            data-state={row.getIsSelected() && "selected"}
          >
            {row.getVisibleCells().map((cell) => (
              <TableCell key={cell.id}>
                {flexRender(cell.column.columnDef.cell, cell.getContext())}
              </TableCell>
            ))}
          </TableRow>
        ))
      ) : (
        <TableRow>
          <TableCell colSpan={mergedColumns.length} className="h-24 text-center">
            {emptyMessage ?? labels.noResults}
          </TableCell>
        </TableRow>
      )}
    </TableBody>
  );
}

function DataTablePagination() {
  const { table, labels, enableRowSelection, pageSizeOptions, loading } = useDataTable();
  const totalPages = table.getPageCount();
  const displayTotalPages = loading && totalPages === 0 ? 1 : totalPages;
  const currentPage = table.getState().pagination.pageIndex + 1;

  return (
    <div className="flex flex-wrap items-center justify-between gap-4 py-4">
      <div className="flex items-center gap-4">
        <div className="text-sm text-muted-foreground">
          {labels.pageXofY(currentPage, displayTotalPages)}
        </div>
        {enableRowSelection && table.getFilteredSelectedRowModel().rows.length > 0 && (
          <div className="text-sm font-medium text-primary">
            {table.getFilteredSelectedRowModel().rows.length} sélectionné(s)
          </div>
        )}
        <Select
          value={String(table.getState().pagination.pageSize)}
          onValueChange={(v) => table.setPageSize(Number(v))}
          disabled={loading}
        >
          <SelectTrigger className="h-8 w-28">
            <span className="flex-1 text-left">{table.getState().pagination.pageSize}{labels.itemsPerPage}</span>
          </SelectTrigger>
          <SelectContent position="popper">
            {pageSizeOptions.map((size) => (
              <SelectItem key={size} value={String(size)}>{size}{labels.itemsPerPage}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="flex flex-wrap items-center gap-1">
        <Button
          variant="outline"
          size="sm"
          onClick={() => table.previousPage()}
          disabled={loading || !table.getCanPreviousPage()}
        >
          {labels.previous}
        </Button>
        {displayTotalPages > 1 &&
          (() => {
            let ellipsisCount = 0;
            return getPageNumbers(currentPage, displayTotalPages).map((page) => {
              const key = page === "..." ? `ellipsis-${++ellipsisCount}` : `page-${page}`;
              return page === "..." ? (
                <span key={key} className="px-1 text-muted-foreground">...</span>
              ) : (
                <Button
                  key={key}
                  variant={page === currentPage ? "default" : "outline"}
                  size="sm"
                  className="min-w-9"
                  onClick={() => table.setPageIndex(page - 1)}
                  disabled={loading}
                >
                  {page}
                </Button>
              );
            });
          })()}
        <Button
          variant="outline"
          size="sm"
          onClick={() => table.nextPage()}
          disabled={loading || !table.getCanNextPage()}
        >
          {labels.next}
        </Button>
      </div>
    </div>
  );
}

export function DataTable<TData, TValue>(props: DataTableProps<TData, TValue>) {
  return (
    <DataTableProvider {...props}>
      <DataTableToolbar />
      <div className="rounded-md border overflow-x-auto" data-testid="data-table">
        <Table>
          <DataTableHeader />
          <DataTableBody />
        </Table>
      </div>
      <DataTablePagination />
    </DataTableProvider>
  );
}
