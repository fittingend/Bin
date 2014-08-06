#include "pitches.h"

int value;

int buzzer = 10;
int vib = 11;

int red = 9;
int green = 5;
int blue = 6;
 
int melody[] = {
   NOTE_G3,NOTE_C4};
   
int melody_done[] = {
 NOTE_C4,  NOTE_G3};
   
int noteDurations[] = {
  4, 4 };
 
void setup()
{
 Serial.begin(9600);
 pinMode(buzzer, OUTPUT);
 pinMode(vib, OUTPUT);
 pinMode(red, OUTPUT);
 pinMode(green, OUTPUT);
 pinMode(blue, OUTPUT);
 color(255,255,0);
 
 Serial.flush();
 }
 
 
void loop()
{
 delay(100);
}
 
void serialEvent() // To check if there is any data on the Serial line
 
{
while (Serial.available())
 {
value = Serial.parseInt();

switch(value){
  case 1: //not ready_orange LED on
  color(255,255,0);
  
  break;
  
  case 2: //ready for exercise _ blue LED with buzzer
  color(0, 0, 255); //blue LED on
  digitalWrite(vib, HIGH);
  delay(500);
  digitalWrite(vib, LOW);
  for (int thisNote = 0; thisNote < 2; thisNote++) {

    // to calculate the note duration, take one second 
    // divided by the note type.
    //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
    int noteDuration = 1000/noteDurations[thisNote];
    tone(10, melody[thisNote],noteDuration);

    // to distinguish the notes, set a minimum time between them.
    // the note's duration + 30% seems to work well:
    int pauseBetweenNotes = noteDuration * 1.30;
    delay(pauseBetweenNotes);
    // stop the tone playing:
    noTone(10);  
    // buzzer on for a while
  }
  
  break;
  
  
  case 3: //do it again_ red LED blinks with vibration
  color(255, 0, 0);
  digitalWrite(vib, HIGH);
  delay(500);
  color(0, 0, 0);
  digitalWrite(vib, LOW);
  delay(500);
  color(255, 0, 0);
  digitalWrite(vib, HIGH);
  delay(500);
  color(0, 0, 0);
  digitalWrite(vib, LOW);
  break;
  
  
  case 4: //blue LED
  color(0, 0, 255);
  break;
  
  case 5: //blue LED blinks with sound 
  color(0, 0, 255); 
  digitalWrite(vib, HIGH);
  delay(500);
  digitalWrite(vib, LOW);
  for (int thisNote = 0; thisNote < 2; thisNote++) {

    // to calculate the note duration, take one second 
    // divided by the note type.
    //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
    int noteDuration = 1000/noteDurations[thisNote];
    tone(10, melody_done[thisNote],noteDuration);

    // to distinguish the notes, set a minimum time between them.
    // the note's duration + 30% seems to work well:
    int pauseBetweenNotes = noteDuration * 1.30;
    delay(pauseBetweenNotes);
    // stop the tone playing:
    noTone(10);  
    // buzzer on for a while
}
 
 break;
 
Serial.println("Succesfully received.");
 }
}
}
void color (unsigned char redVal, unsigned char greenVal, unsigned char blueVal)     // the color generating function
{	 
          analogWrite(red, 255-redVal);	 
          analogWrite(blue, 255-blueVal);
          analogWrite(green, 255-greenVal);
}	 
