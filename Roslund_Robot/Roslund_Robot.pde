int SerialData = 0;
int MotorSpeed1 = 127;
int MotorSpeed2 = 127;
int MotorSpeed3 = 127;
int MotorSpeed4 = 127;
int ArmUp = 0;
int ArmDown = 0;
int Gripper = 0; 
int BackPiston = 0;
int SidePiston = 0;
int Spare =0;
int counter = 0;
const int MOTOR1 = 3;
const int MOTOR2 = 6;
const int MOTOR3 = 10;
const int MOTOR4 = 11;
const int ARMUP = 2;
const int ARMDOWN = 4;
const int GRIPPER = 7;
const int BACKPISTON = 8;
const int SIDEPISTON = 12;
const int SPARE = 13;
unsigned long timer=0;
unsigned long timer2=0;

void setup() 
{
  pinMode(MOTOR1,OUTPUT);  
  pinMode(MOTOR2,OUTPUT);
  pinMode(MOTOR3,OUTPUT);
  pinMode(MOTOR4,OUTPUT);
  pinMode(ARMUP,OUTPUT);
  pinMode(ARMDOWN,OUTPUT);
  pinMode(GRIPPER,OUTPUT);
  pinMode(BACKPISTON,OUTPUT);
  pinMode(SIDEPISTON,OUTPUT);
  pinMode(SPARE,OUTPUT);
  Serial.begin(57600);
}

void loop()
{
  timer2=millis();
  if ((timer2-timer)>500ul)
  {
    MotorSpeed1=127;
    MotorSpeed2=127;
    MotorSpeed3=127;
    MotorSpeed4=127;
    ArmUp = 0;
    ArmDown = 0;
    Gripper = 0;
    BackPiston = 0;
    SidePiston = 0;
    Spare = 0;
    counter = 0;
    run();
  }
  
  while ( Serial.available()>0)
  {
    timer=millis();
    counter++;  
    SerialData = Serial.read();  
    if(SerialData == 10 && counter == 11) // 10 shows the end of a data frame
    {
      run();        // data is valid => Run command
      counter = 0;  // it is ready for new data frame    
      Serial.println("Test Run");  
    }   
    if(counter ==1) // first byte in data frame is Motor1 Speed value
    {
      MotorSpeed1 = SerialData;   
      Serial.println("test 1");    
    }
    else if(counter ==2) // second byte in data frame is Motor2 Speed value
    {
      MotorSpeed2 = SerialData;    
      Serial.println("test 2");       
    }
    else if(counter ==3)
    {
      MotorSpeed3 = SerialData;
    }
    else if(counter ==4)
    {
      MotorSpeed4 = SerialData;
    }
    else if(counter ==5)
    {
      ArmUp = SerialData;
    }
    else if(counter ==6)
    {
      ArmDown = SerialData;
    }
    else if(counter ==7)
    {
      Gripper = SerialData;
    }
    else if(counter ==8)
    {
      BackPiston = SerialData;
    }
    else if(counter == 9)
    {
      SidePiston = SerialData;
    }
    else if(counter == 10)
    {
      Spare = SerialData;
      Serial.println("test 10");
    }
    else if(counter >11) // if counter is greater than 5 something is wrong 
    {                   // Reset values
      MotorSpeed1 = 127;
      MotorSpeed2 = 127;
      MotorSpeed3 = 127;
      MotorSpeed4 = 127;
      ArmUp = 0;
      ArmDown = 0;
      Gripper = 0;
      BackPiston = 0;
      SidePiston = 0;
      Spare = 0;
      counter = 0;
      Serial.println("reset");
      run();
    }
      //Serial.print(SerialData, BYTE); // for testing  
  }
}
void run ()
{
   MotorSpeed1=map(MotorSpeed1,0,255,34,154);
   MotorSpeed2=map(MotorSpeed2,0,255,64,184);
   MotorSpeed3=map(MotorSpeed3,0,255,34,154);
   MotorSpeed4=map(MotorSpeed4,0,255,34,154);
   
   analogWrite(MOTOR1,MotorSpeed1);//Serial.println(MotorSpeed1);  
   analogWrite(MOTOR2,MotorSpeed2);//Serial.println(MotorSpeed2); 
   analogWrite(MOTOR3,MotorSpeed3);//Serial.println(MotorSpeed3); 
   analogWrite(MOTOR4,MotorSpeed4);//Serial.println(MotorSpeed4); 
   
   digitalWrite(ARMUP,ArmUp);
   digitalWrite(ARMDOWN,ArmDown);
   digitalWrite(GRIPPER,Gripper);
   digitalWrite(BACKPISTON,BackPiston);
   digitalWrite(SIDEPISTON,SidePiston);
   digitalWrite(SPARE,Spare);
}
