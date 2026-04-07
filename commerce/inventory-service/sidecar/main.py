from fastapi import FastAPI
from typing import List
from agents.reorder_agent import ReorderAgent, InventoryItem

app = FastAPI(title="Inventory AI Sidecar")
agent = ReorderAgent()


@app.get("/health")
def health():
    return {"status": "UP", "circuit_open": agent.circuit_open}


@app.post("/reorder/recommend")
def recommend_reorder(items: List[InventoryItem]):
    recommendations = agent.recommend(items)
    return {"recommendations": [r.dict() for r in recommendations]}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8201)
