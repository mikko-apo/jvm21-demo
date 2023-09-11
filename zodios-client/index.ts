import * as greet from "./greet-zodios-client"

const client = greet.createApiClient("http://localhost:8080");

async function doStuff(){
    const result = await client.get("/greet")
    console.log(result)
}

doStuff().catch(e => console.error(e))
