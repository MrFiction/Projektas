#include <DallasTemperature.h>

#include <OneWire.h>

#define ONE_WIRE_BUS 2
// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);
// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature sensors(&oneWire);
int consoleRead;//reles testas is konsoles
int temperature;
int test,test2;
void setup(void)
{
  Serial.begin(9600); //Begin serial communication
  Serial.println("Arduino Digital Temperature // Serial Monitor Version"); //Print a message
  sensors.begin();
  pinMode(7,OUTPUT);//rele
  pinMode(6,OUTPUT);//raudona
  pinMode(3,OUTPUT);//melyna
}

void loop(void)
{ 
  // Send the command to get temperatures
  sensors.requestTemperatures();  
  temperature = (int)sensors.getTempCByIndex(0);
  //test = (int)Serial.read();
  //Serial.println(test);
  //Serial.print("Temperature is: ");
  if(test > 0)
  {
     test2 =test;
     //Serial.println(test2);
  
  }
  Serial.println(temperature); // Why "byIndex"? You can have more than one IC on the same bus. 0 refers to the first IC on the wire
  //Update value every 1 sec.
  //a = Serial.read() - '0';
  if(temperature >= 80)
  {
    digitalWrite(7,LOW);
    
  }
  else 
  digitalWrite(7,HIGH);
  analogWrite(6,((int)sensors.getTempCByIndex(0)*2.55));
  analogWrite(3,(255-(int)(sensors.getTempCByIndex(0)*2.55)));
  
  delay(1000);
}
