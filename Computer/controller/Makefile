# Compiler and linker options
LIBS += -lSDL -lpthread
INCLUDES += -I. -I../common/ -I/usr/include/SDL
OPTS += -Wall -g
CC ?= gcc

# Source file layout
BINARY ?= controller
LOCAL_OBJ = controller.o joystick.o 
LOCAL_OBJ_ROSLUND = controller_events_roslund.o

COMMON = ../common
COMMON_OBJ = robot_comm.o robot_log.o \
		 robot_queue.o \
		 timer.o \
		 profile.o

# Begin derived variables
LOCAL_SRC = $(LOCAL_OBJ:%.o=%.c)
COMMON_SRC = $(COMMON_OBJ:%.o=$(COMMON)/%.c)
CFLAGS += $(INCLUDES) $(OPTS)
SRC = $(LOCAL_SRC) $(COMMON_SRC)
OBJ = $(LOCAL_OBJ) $(COMMON_OBJ)
OBJ_ROSLUND = $(OBJ) $(LOCAL_OBJ_ROSLUND)

.PHONY: all
all: $(BINARY)-roslund 

.PHONY: roslund
roslund: $(BINARY)-roslund

$(BINARY)-roslund: $(OBJ_ROSLUND)
	$(CC) $(CFLAGS) $(LIBS) $(OBJ_ROSLUND) -o $(BINARY)-roslund

%.o: $(COMMON)/%.c
	$(CC) $(CFLAGS) -c $< -o $@
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

.PHONY: clean
clean:
	-rm -f $(OBJ) $(OBJ_ROSLUND) $(OBJ_COLONEL) $(OBJ_FENRIR) $(BINARY)-roslund $(BINARY)-colonel $(BINARY)-fenrir $(BINARY)
