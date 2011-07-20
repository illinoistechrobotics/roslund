#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "robot_queue.h"

void test_none();
void test_one();
void test_mid();
void test_overflow();
void test_wait();
int assert_equal(int expect, int given, char *msg);
int assert_false(int given, char *msg);
int assert_true(int given, char *msg);

const robot_event ev[15] = { {1, 2, 3}, {2, 3, 4}, {3, 4, 5},
			     {4, 5, 6}, {5, 6, 7}, {6, 7, 8},
			     {7, 8, 9}, {8, 9, 10}, {9, 10, 11},
			     {10, 11, 12}, {11, 12, 13}, {12, 13, 14},
			     {13, 14, 15}, {14, 15, 16}, {15, 16, 17} };
int main() {

	printf("test_none()\n");
	test_none();
	printf("test_one()\n");
	test_one();
	printf("test_mid()\n");
	test_mid();
	printf("test_overflow()\n");
	test_overflow();
	printf("test_wait()\n");
	test_wait();

	// if we got this far say yay!
	printf("Yoohoo! no tests failed!\n");
	printf("U R 1337 haxx0rz.\n");
			
	return 0;
}

void test_none() {
	robot_queue q;
	robot_event temp_ev;

	robot_queue_create(&q);
	assert_equal(0, robot_queue_get_length(&q), "Size not 0");

	assert_false(robot_queue_dequeue(&q, &temp_ev), "successfully dequeued on 0 queue");
	assert_equal(0, robot_queue_get_length(&q), "Size not 0 after dequeue");

	robot_queue_destroy(&q);
}

void test_one() {
	robot_queue q;
	robot_event temp_ev;

	robot_queue_create(&q);

	assert_true(robot_queue_enqueue(&q, &ev[0] ), "Enqueue failed.");
	assert_equal(1, robot_queue_get_length(&q), "Size not 1 after enqueue.");
	
	assert_true(robot_queue_dequeue(&q, &temp_ev), "Dequeue Failed.");
	assert_equal(ev[0].command, temp_ev.command, "Dequeued item incorrect.");

	assert_equal(0, robot_queue_get_length(&q), "Size not 0 after last dequeue.");

	robot_queue_destroy(&q);
}

void test_mid() {
	robot_queue q;
	robot_event temp_ev;

	robot_queue_create(&q);

	assert_true(robot_queue_enqueue(&q, &ev[0] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[1] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[2] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[3] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[4] ), "Enqueue failed.");

	assert_equal(5, robot_queue_get_length(&q), "Size not 5 after 5 enqueues.");

	assert_true(robot_queue_dequeue(&q, &temp_ev), "Dequeue Failed.");
	assert_equal(ev[0].command, temp_ev.command, "Dequeued item incorrect.");
	assert_equal(4, robot_queue_get_length(&q), "Size not 4 after dequeueing an item.");

	assert_true(robot_queue_dequeue(&q, &temp_ev), "Dequeue Failed.");
	assert_equal(ev[1].command, temp_ev.command, "Dequeued item incorrect.");
	assert_equal(3, robot_queue_get_length(&q), "Size not 3 after dequeueing an item.");

	assert_true(robot_queue_dequeue(&q, &temp_ev), "Dequeue Failed.");
	assert_equal(ev[2].command, temp_ev.command, "Dequeued item incorrect.");
	assert_equal(2, robot_queue_get_length(&q), "Size not 2 after dequeueing an item.");

	assert_true(robot_queue_dequeue(&q, &temp_ev), "Dequeue Failed.");
	assert_equal(ev[3].command, temp_ev.command, "Dequeued item incorrect.");
	assert_equal(1, robot_queue_get_length(&q), "Size not 1 after dequeueing an item.");

	assert_true(robot_queue_dequeue(&q, &temp_ev), "Dequeue Failed.");
	assert_equal(ev[4].command, temp_ev.command, "Dequeued item incorrect.");
	assert_equal(0, robot_queue_get_length(&q), "Size not 0 after dequeueing an item.");

	robot_queue_destroy(&q);
}

void test_overflow() {
	robot_queue q;
	int i; // your friendly neighbourhood iterator

	robot_queue_create(&q);

	for(i = 0; i < 35; ++i) {
		assert_true(robot_queue_enqueue(&q, &ev[i] ), "Enqueue failed.");
		printf("%d\n", i);
	}

	assert_equal(QUEUE_SIZE, robot_queue_get_length(&q), "Size not QUEUE_SIZE after 35 enqueues.");

	robot_queue_destroy(&q);
}

void test_wait() {
	robot_queue q;
	robot_event temp_ev;

	robot_queue_create(&q);

	assert_true(robot_queue_enqueue(&q, &ev[0] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[1] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[2] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[3] ), "Enqueue failed.");
	assert_true(robot_queue_enqueue(&q, &ev[4] ), "Enqueue failed.");

	assert_true(robot_queue_wait_event(&q, &temp_ev), "Wait event failed");
	assert_equal(ev[0].command, temp_ev.command, "Dequeued item incorrect.");
	assert_true(robot_queue_wait_event(&q, &temp_ev), "Wait event failed");
	assert_equal(ev[1].command, temp_ev.command, "Dequeued item incorrect.");
	assert_true(robot_queue_wait_event(&q, &temp_ev), "Wait event failed");
	assert_equal(ev[2].command, temp_ev.command, "Dequeued item incorrect.");
	assert_true(robot_queue_wait_event(&q, &temp_ev), "Wait event failed");
	assert_equal(ev[3].command, temp_ev.command, "Dequeued item incorrect.");
	assert_true(robot_queue_wait_event(&q, &temp_ev), "Wait event failed");
	assert_equal(ev[4].command, temp_ev.command, "Dequeued item incorrect.");

	printf("Now we wait... forevah!!!\n");
	assert_true(robot_queue_wait_event(&q, &temp_ev), "Wait event failed");
	printf("We woke up, oops that shouldn't happen.\n");

	robot_queue_destroy(&q);
}

int assert_equal(int expect, int given, char *msg) {
	if(expect != given) {
		printf("%s\n", msg);
		exit(1);
	}
	return 1;
}

int assert_false(int given, char *msg) {
	if(given) {
		printf("%s\n", msg);
		exit(1);
	}
	return 1;
}

int assert_true(int given, char *msg) {
	if(!given) {
		printf("%s\n", msg);
		exit(1);
	}
	return 1;
}
