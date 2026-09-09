import { setupServer } from "msw/node";
import { delay } from "msw";
import { handlers } from "./handlers";

let globalDelay = 0;

export function setResponseDelay(ms: number) {
  globalDelay = ms;
}

export function clearResponseDelay() {
  globalDelay = 0;
}

export const server = setupServer(...handlers);

server.events.on("request:start", async () => {
  if (globalDelay > 0) {
    await delay(globalDelay);
  }
});
