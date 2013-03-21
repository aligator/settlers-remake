package jsettlers.logic.map.newGrid.partition.manager.materials.requests;

import java.util.Arrays;
import java.util.Iterator;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.material.EPriority;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.collections.list.DoubleLinkedList;

/**
 * This class is a simple priority queue for material requests. The possible priorities are specified in the {@link EPriority} enum.
 * 
 * @author Andreas Eberle
 * 
 */
public final class SimpleMaterialRequestPriorityQueue extends AbstractMaterialRequestPriorityQueue {
	private static final long serialVersionUID = 4856036773080549412L;

	private final DoubleLinkedList<MaterialRequestObject> queues[] = DoubleLinkedList.getArray(EPriority.NUMBER_OF_PRIORITIES);

	@Override
	protected DoubleLinkedList<MaterialRequestObject> getQueue(EPriority priority, EBuildingType buildingType) {
		return queues[priority.ordinal];
	}

	@Override
	protected MaterialRequestObject getRequestForPrio(int prio) {
		DoubleLinkedList<MaterialRequestObject> queue = queues[prio];

		int numberOfElements = queue.size();

		for (int handledElements = 0; handledElements < numberOfElements; handledElements++) {
			MaterialRequestObject result = queue.getFront();

			int inDelivery = result.inDelivery;
			int stillNeeded = result.getStillNeeded();

			// if the request is done
			if (stillNeeded <= 0) {
				result.requestQueue = null;
				queue.popFront(); // remove the request
				numberOfElements--;
			}

			// if all needed are in delivery, or there can not be any more in delivery
			else if (stillNeeded <= inDelivery || inDelivery >= result.getInDeliveryable()) {
				queue.pushEnd(queue.popFront()); // move the request to the end.
			}

			// everything fine, take this request
			else {
				if (result.isRoundRobinRequest()) {
					queue.pushEnd(queue.popFront()); // put the request to the end of the queue.
				}

				return result;
			}
		}

		return null;
	}

	@Override
	public void moveObjectsOfPositionTo(ShortPoint2D position, AbstractMaterialRequestPriorityQueue newAbstractQueue) {
		assert newAbstractQueue instanceof SimpleMaterialRequestPriorityQueue : "can't move positions between diffrent types of queues.";

		SimpleMaterialRequestPriorityQueue newQueue = (SimpleMaterialRequestPriorityQueue) newAbstractQueue;

		for (int queueIdx = 0; queueIdx < queues.length; queueIdx++) {
			Iterator<MaterialRequestObject> iter = queues[queueIdx].iterator();
			while (iter.hasNext()) {
				MaterialRequestObject curr = iter.next();
				if (curr.getPos().equals(position)) {
					iter.remove();
					newQueue.queues[queueIdx].pushEnd(curr);
				}
			}
		}
	}

	@Override
	public void mergeInto(AbstractMaterialRequestPriorityQueue newAbstractQueue) {
		assert newAbstractQueue instanceof SimpleMaterialRequestPriorityQueue : "can't move positions between diffrent types of queues.";

		SimpleMaterialRequestPriorityQueue newQueue = (SimpleMaterialRequestPriorityQueue) newAbstractQueue;

		for (int queueIdx = 0; queueIdx < queues.length; queueIdx++) {
			queues[queueIdx].mergeInto(newQueue.queues[queueIdx]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleMaterialRequestPriorityQueue other = (SimpleMaterialRequestPriorityQueue) obj;
		if (!Arrays.equals(queues, other.queues))
			return false;
		return true;
	}
}
