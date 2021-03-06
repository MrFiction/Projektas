#include <DallasTemperature.h>
#include <OneWire.h>
#include <stdio.h>

#define ONE_WIRE_BUS 2

// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature sensors(&oneWire);
int temperature; //Temperatura is temperaturos daviklio
int input; //Kintamasis kuris saugo skaiciu iki 255(gauta is programeles)
bool boiled; //loginis kintamasis skirtas patikrinti, ar vanduo uzvyre
int state; //Bluetooth modulio prisijungimo statusas

void setup(void)
{
  Serial.begin(9600); //Begin serial communication
  sensors.begin();
  pinMode(7,OUTPUT);//rele
  pinMode(8,INPUT);//Bluetoth state
  pinMode(6,OUTPUT);//raudonas LED
  pinMode(3,OUTPUT);//melynas LED
}

void loop(void)
{ 
  //Gauti temperatura is sensoriaus
  sensors.requestTemperatures();
  temperature = (int)sensors.getTempCByIndex(0);
  
  input = Serial.read();
  Serial.println(temperature);

  //Apsauga --------------------------------------------------------------------------------------------------------------------------------------------
  
  state = digitalRead(8);
  if (state == 0)
    digitalWrite(7,LOW);

  //Injungti arba isjungti ------------------------------------------------------------------------------------------------------------------------------

  if (temperature < 90)
  {
    if(input == 1)
      digitalWrite(7,HIGH);
  }
  if(input == 0)
    digitalWrite(7,LOW);

  //Kaitinti iki nurodytos temperaturos (gaunamas skaicius nuo 100 iki 200 yra temperatura) -------------------------------------------------------------
  
  if (((temperature < 90) && ((input >= 160) && (input <= 190))))
  {
    digitalWrite(7,HIGH);
    boiled = true;
    while (boiled)
    {
      sensors.requestTemperatures();
      input = Serial.read();
      temperature = (int)sensors.getTempCByIndex(0);
      Serial.println(temperature);
      state = digitalRead(8);
      if (state == 0)
      {
        digitalWrite(7,LOW);
        boiled = false;
      }
      if (input == 0)
      {
        digitalWrite(7,LOW);
        boiled = false;
      }
      delay(1000);
    }
  }
  
  delay(1000);
}
