#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <SDL/SDL.h>

struct point
{
	int input;
	int output;
};

/* These maps are a set of points defining the joystick input / arduino output mapping function.
   Linear interpolation is done for points not explicitly specified.
   These points must be sorted by input, and duplicate inputs are not allowed.
   */
static point x_axis_map[] = { {-32768, 50}, {-31744,  50}, { -1024,  60},
	{  1024, 60}, { 31744,  70}, { 32767,  70} };

static point y_axis_map[] =  { {-32768, 180}, {-31744, 180}, { -1024, 90},
	{  1024,  90}, { 31744,   0}, { 32767,  0} };

static const int Lx_axis = 0;
static const int Ly_axis = 1;
static const int Rx_axis = 2;
static const int Ry_axis = 3;
static const int but_1 = 0;
static const int but_2 = 1;
static const int but_3 = 2;
static const int but_4 = 3;
static const int but_5 = 4;
static const int but_6 = 5;
static const int but_7 = 6;
static const int but_8 = 7;
static const int but_9 = 8;
static const int but_10 = 9;
static const int Lx_bit = 0;
static const int Ly_bit = 1;
static const int Rx_bit = 2;
static const int Ry_bit = 3;
static const int b1_bit = 4;
static const int b2_bit = 5;
static const int b3_bit = 6;
static const int b4_bit = 7;
static const int b5_bit = 8;
static const int b6_bit = 9;
static const int b7_bit = 10;
static const int b8_bit = 11;
static const int b9_bit = 12;
static const int b10_bit = 13;
static const int size_bit = 14;
static const int endframe = 10;
static const int err_bit = 20;

int mot1=0;
int mot2=0;
int mot3=0;
int mot4=0; 

int turbo=0;

int max = 0;

//Open serial port to arduino, xbee, ect.

static int open_serial_port (const char * port)
{
	int fd = open(port, O_RDWR, 0);
	if (fd < 0)
	{
		printf("Unable to open serial port %s: ", port);
		perror("");
		return -1;
	}    
	return fd;
}

//Open Joystick 
static SDL_Joystick * open_joystick ()
{
	if (SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_VIDEO) < 0)
	{
		printf("SDL_Init: %s", SDL_GetError());
		return NULL;
	}

	int num_joysticks = SDL_NumJoysticks();
	if (num_joysticks == 0)
	{
		printf("No joysticks found\n");
		return NULL;
	}
	printf("%d joystick(s) found\n", num_joysticks);
	printf("Using joystick \"%s\"\n", SDL_JoystickName(0));

	SDL_Joystick * joystick = SDL_JoystickOpen(0);
	if (!joystick)
	{
		printf("SDL_JoystickOpen: %s", SDL_GetError());
		return NULL;
	}

	SDL_JoystickEventState(SDL_ENABLE);
	return joystick;
}

static int apply_map (int input, const point points[], int num_points)
{
	/* Do a binary search to find the 2-point interval */
	int start = 0;
	int end = num_points - 1;
	while (start + 1 != end)
	{
		int index = (start + end) / 2;
		if (input < points[index].input)
		{
			end = index;
		}
		else
		{
			start = index;
		}
	}
	/* Do linear interpolation to determine output */
	return points[start].output + (points[end].output - points[start].output) * (input - points[start].input)
		/ (points[end].input - points[start].input);
};

/*get input from joystick like axis change, button press, button release
  Joystick Map
  Left x-axis 0
  Left y-axis 1
  Right x-axis 2
  Right y-axis 3
  Buttons
  1=0
  2=1
  ...
  9=8
  10=9
  */

static int scale(int input){
	return (input + 32768) * (126 + 127) / (32767 + 32768) -126;
}


static int get_input (int input[err_bit])
{
	SDL_Event event;
	while (SDL_PollEvent(&event))
	{
		switch (event.type)
		{
			case SDL_JOYAXISMOTION:
				//This is for all four axis and if the axis value changes 
				//then the input array is changed
				if (event.jaxis.axis == Lx_axis) 
				{
					input[Lx_bit] = scale(event.jaxis.value);
				}
				if (event.jaxis.axis == Ly_axis) 
				{
					input[Ly_bit] = scale(event.jaxis.value);
				}
				if (event.jaxis.axis == Rx_axis) 
				{
					input[Rx_bit] = scale(event.jaxis.value);
				}

				if (event.jaxis.axis == Ry_axis) 
				{
					input[Ry_bit] = scale(event.jaxis.value);
				}
				break;
			case SDL_JOYBUTTONDOWN:
				//When button is pressed the value changes to 1
				if(event.jbutton.button == but_1) //button 1
				{
					input[b1_bit] = 1;
				}		
				if(event.jbutton.button == but_2) //button 2
				{
					input[b2_bit] = input[b2_bit]+1;
				}
				if(event.jbutton.button == but_3) //button 3
				{
					input[b3_bit] = 1;
				}
				if(event.jbutton.button == but_4) //button 4
				{
					input[b4_bit] = 1;
				}
				if(event.jbutton.button == but_6) //arm up
				{
					input[b5_bit] = 1;
				}
				if(event.jbutton.button == but_8) //arm down
				{
					input[b6_bit] = 1;
				}
				if(event.jbutton.button == but_5) //turbo 1
				{
					input[b7_bit] = turbo++;
				}
				if(event.jbutton.button == but_7) //turbo 2
				{
					input[b8_bit] = turbo++;
				}
				if(event.jbutton.button == but_9) //button 9
				{
					input[b9_bit] = 1;
				}
				if(event.jbutton.button == but_10) //button 10
				{
					input[b10_bit] = 1;
				}				
				break;
			case SDL_JOYBUTTONUP:
				//when button is released the value chances to 0
				if(event.jbutton.button == but_1) //button 1
				{
					input[b1_bit] = 0;
				}
				if(event.jbutton.button == but_2) //button 2
				{
					//input[b2_bit] = 0;
				}
				if(event.jbutton.button == but_3) //button 3
				{
					input[b3_bit] = 0;
				}
				if(event.jbutton.button == but_4) //button 4
				{
					input[b4_bit] = 0;
				}
				if(event.jbutton.button == but_6) //arm up
				{
					input[b5_bit] = 0;
				}
				if(event.jbutton.button == but_8) //arm down
				{
					input[b6_bit] = 0;
				}
				if(event.jbutton.button == but_5) //turbo 1
				{
					input[b7_bit] = turbo--;
				}
				if(event.jbutton.button == but_7) //turbo 2
				{
					input[b8_bit] = turbo--;
				}
				if(event.jbutton.button == but_9) //button 9
				{
					input[b9_bit] = 0;
				}
				if(event.jbutton.button == but_10) //button 10
				{
					input[b10_bit] = 0;
				}		
				break;	
			case SDL_QUIT:
				exit(EXIT_SUCCESS);
				break;
		}
	}
	return 0;
}

//send data to arduino
static int send_output (int port_fd, const int output[err_bit])
{
	//char output_buf[] = {255, mot1, mot2, mot3, mot4}; 
	//write(port_fd, mot1, 1);
	//write(port_fd, output_buf[1], 1);
	//write(port_fd, output_buf[2], 1);
	//write(port_fd, output_buf[3], 1);
	//write(port_fd, output_buf[4], 1);
	//return 0;

	char output_buf[] = {mot1,mot2,mot3,mot4,output[b5_bit],output[b6_bit],output[b2_bit],output[b4_bit],output[b1_bit],output[b3_bit],10};               
	if (write(port_fd, output_buf, sizeof(output_buf)) < 0)
	{
		perror("write");
		return -1;
	}
	/*
	   usleep(10000);
	   char output_buf2[] = {mot3,mot4};               
	   if (write(port_fd, output_buf2, sizeof(output_buf2)) < 0)
	   {
	   perror("write");
	   return -1;
	   }    
	   */
	return 0;
}

int main (int argc, char * argv[])
{
	const char * port = NULL;
	if (argc < 2)
	{
		printf("No serial port given; not outputting to serial\n");
	}
	else
	{
		port = argv[1];
	}

	int port_fd;
	if (port)
	{
		port_fd = open_serial_port(port);
		if (port_fd < 0)
		{
			return EXIT_FAILURE;
		}
	}

	SDL_Joystick * joystick = open_joystick();
	if (!joystick)
	{
		return EXIT_FAILURE;
	}

	int input[err_bit] = {};
	int output[err_bit] = {};

	while (true)
	{
		get_input(input);
		max=0;
		mot1 = -(input[Ly_bit] - input[Lx_bit] + input[Rx_bit]);
		mot2 = (input[Ly_bit] + input[Lx_bit] + input[Rx_bit]);
		mot3 = -(input[Ly_bit] - input[Lx_bit] - input[Rx_bit]);
		mot4 = (-input[Ly_bit] - input[Lx_bit] + input[Rx_bit]);

		if(abs(mot1) >  127 || abs(mot2) > 127 || abs(mot3) > 127 || abs(mot4) > 127) {
			max = 0;
			if(abs(mot1)>max)
				max = abs(mot1);
			if(abs(mot2)>max)
				max = abs(mot2);
			if(abs(mot3)>max)
				max = abs(mot3);
			if(abs(mot4)>max)
				max = abs(mot4);
			mot1=(int)((float)mot1/max*127);
			mot2=(int)((float)mot2/max*127);
			mot3=(int)((float)mot3/max*127);
			mot4=(int)((float)mot4/max*127);
		}

		mot1 = (mot1*2)/(4 - turbo);
		mot2 = (mot2*2)/(4 - turbo);
		mot3 = (mot3*2)/(4 - turbo);
		mot4 = (mot4*2)/(4 - turbo);

		mot1=mot1+127;
		mot2=mot2+127;
		mot3=mot3+127;
		mot4=mot4+127;

		output[b1_bit] = input[b1_bit];
		output[b2_bit] = input[b2_bit]%2;
		output[b3_bit] = input[b3_bit];
		output[b4_bit] = input[b4_bit];
		output[b5_bit] = input[b5_bit];
		output[b6_bit] = input[b6_bit];


		if (port)
		{
			send_output(port_fd, output);
		}
		printf("%8d %8d %8d  %8d\r", mot1, mot2, mot3, mot4);
		fflush(stdout);
		usleep(10000);
	}
}
