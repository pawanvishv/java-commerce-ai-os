from fastapi import FastAPI, HTTPException
from typing import List
from agents.driver_agent import (
    DriverAssignmentAgent, Driver
)

app = FastAPI(title="Order AI Sidecar")
agent = DriverAssignmentAgent()


@app.get("/health")
def health():
    return {"status": "UP", "circuit_open": agent.circuit_open}


@app.post("/driver/assign")
def assign_driver(order_id: str, drivers: List[Driver]):
    result = agent.assign(order_id, drivers)
    if not result:
        raise HTTPException(
            status_code=404,
            detail="No drivers available"
        )
    return result.dict()


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8202)
