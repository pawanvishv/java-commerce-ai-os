from pydantic import BaseModel
from typing import List, Optional


class Driver(BaseModel):
    driver_id: str
    name: str
    distance_km: float
    rating: float
    active_deliveries: int


class DriverAssignment(BaseModel):
    order_id: str
    assigned_driver_id: str
    assigned_driver_name: str
    estimated_pickup_minutes: int
    reason: str


class DriverAssignmentAgent:
    """
    AI agent for driver assignment.
    Fallback: distance ASC (nearest driver first)
    Circuit breaker: 50% failure over 10 calls -> OPEN
    """

    def __init__(self):
        self.failure_count = 0
        self.call_count = 0
        self.circuit_open = False

    def assign(self, order_id: str,
               drivers: List[Driver]) -> Optional[DriverAssignment]:
        if not drivers:
            return None

        self.call_count += 1

        if self.circuit_open:
            return self._fallback_assign(order_id, drivers)

        try:
            result = self._ai_assign(order_id, drivers)
            return result
        except Exception as e:
            self.failure_count += 1
            if self.call_count >= 10 and \
               self.failure_count / self.call_count >= 0.5:
                self.circuit_open = True
                print(f"Circuit breaker OPEN: {e}")
            return self._fallback_assign(order_id, drivers)

    def _ai_assign(self, order_id: str,
                   drivers: List[Driver]) -> DriverAssignment:
        return self._fallback_assign(order_id, drivers)

    def _fallback_assign(self, order_id: str,
                          drivers: List[Driver]) -> DriverAssignment:
        best = min(drivers, key=lambda d: (
            d.distance_km, -d.rating
        ))
        return DriverAssignment(
            order_id=order_id,
            assigned_driver_id=best.driver_id,
            assigned_driver_name=best.name,
            estimated_pickup_minutes=int(best.distance_km * 3),
            reason=f"Nearest available driver "
                   f"({best.distance_km:.1f}km away)"
        )
