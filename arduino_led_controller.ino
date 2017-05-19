#include "U8glib.h"

// u8glib constructor
U8GLIB_PCD8544 u8g(8, 4, 7, 5, 6);

char command;
String string;
String oldColor = "none";
String currentColor = "none";

boolean on = true;

#define ledR 11
#define ledG 10
#define ledB 9

int colors[3] = {0, 255, 0};

void setup()
{
    Serial.begin(9600);
    pinMode(ledR, OUTPUT);
    pinMode(ledG, OUTPUT);
    pinMode(ledB, OUTPUT);

    if ( u8g.getMode() == U8G_MODE_R3G3B2 ) {
        u8g.setColorIndex(255);     // white
    }
    else if ( u8g.getMode() == U8G_MODE_GRAY2BIT ) {
        u8g.setColorIndex(3);         // max intensity
    }
    else if ( u8g.getMode() == U8G_MODE_BW ) {
        u8g.setColorIndex(1);         // pixel on
    }
    else if ( u8g.getMode() == U8G_MODE_HICOLOR ) {
        u8g.setHiColorByRGB(255,255,255);
    }
    ledOn();
    pinMode(8, OUTPUT);
}

void loop()
{
    if(Serial.available() > 0 ) {
        // reset string
        string = "";
    }

    while(Serial.available() > 0) {
        command = ((byte)Serial.read());
        string += command;
    }

    if (string == "ON") {
        on = true;
        ledOn();

    } else if (string == "OFF") {
        on = false;
        ledOff();
    } else if(string.substring(0,1) == "c") {
        int red = string.substring(1,4).toInt();
        int green = string.substring(4,7).toInt();
        int blue = string.substring(7,10).toInt();

        // update color only if color has changed
        if (red != colors[0] || green != colors[1] || blue != colors[2]) {
            colors[0] = red;
            colors[1] = green;
            colors[2] = blue;

            oldColor = currentColor;
            currentColor = "#";
            for(int i=0;i<3;i++) {
                if (colors[i] < 16) {
                    currentColor += "0";
                }
                currentColor += String(colors[i], HEX);
            }
        }

    }

    // draw to display
    u8g.firstPage();
    do {
        draw();
    } while( u8g.nextPage() );

    if(on) {
        ledOn();
    }
}

void ledOn() {
    analogWrite(ledR, colors[0]);
    analogWrite(ledG, colors[1]);
    analogWrite(ledB, colors[2]);
    delay(10);
}

void ledOff() {
    analogWrite(ledR, 0);
    analogWrite(ledG, 0);
    analogWrite(ledB, 0);
    delay(10);
}

void draw() {
    // u8g.setRot180(); // rotate screen
    u8g.setFont(u8g_font_unifont);
    u8g.drawStr( 0, 12, "Old color:");
    u8g.drawStr( 0, 24, oldColor.c_str());
    u8g.drawStr( 0, 36, "New color:");
    u8g.drawStr( 0, 48, currentColor.c_str());
}
