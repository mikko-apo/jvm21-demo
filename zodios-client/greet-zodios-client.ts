import { makeApi, Zodios, type ZodiosOptions } from "@zodios/core";
import { z } from "zod";

const GreetingMessage = z
  .object({ message: z.string() })
  .partial()
  .passthrough();

export const schemas = {
  GreetingMessage,
};

const endpoints = makeApi([
  {
    method: "get",
    path: "/greet",
    alias: "getGreet",
    requestFormat: "json",
    response: z.object({ message: z.string() }).partial().passthrough(),
  },
  {
    method: "get",
    path: "/greet/:name",
    alias: "getGreetName",
    requestFormat: "json",
    parameters: [
      {
        name: "name",
        type: "Path",
        schema: z.string(),
      },
    ],
    response: z.object({ message: z.string() }).partial().passthrough(),
  },
  {
    method: "put",
    path: "/greet/greeting",
    alias: "putGreetgreeting",
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: z.object({}).passthrough(),
      },
    ],
    response: z.void(),
    errors: [
      {
        status: 400,
        description: `JSON did not contain setting for &#x27;greeting&#x27;`,
        schema: z.void(),
      },
    ],
  },
]);

export const api = new Zodios(endpoints);

export function createApiClient(baseUrl: string, options?: ZodiosOptions) {
  return new Zodios(baseUrl, endpoints, options);
}
