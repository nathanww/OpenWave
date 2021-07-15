import clock from "clock";
import document from "document";
import { preferences } from "user-settings";
import * as util from "../common/utils";
import { vibration } from "haptics";
import { me } from "appbit";

let cycles=0;

function randDelay() {
  return Math.floor(Math.random() * 4000);
}

function vibe()
{ 
  if (isVibrating) {
    vibration.stop();
  }
  else {

       vibration.start("ring");
   
  }
  isVibrating=!isVibrating;
  if (freq < 0.1) {
    if (!isVibrating) {
    clearInterval(timerHandle);
    timerHandle=setInterval(vibe,randDelay());
    }
    else {
      clearInterval(timerHandle);
    timerHandle=setInterval(vibe,1000);
    }
  }

}


function constantVibe()
{ 
      vibration.stop();

      vibration.start("ring");
}
me.appTimeoutEnabled = false; // Disable timeout

let freq=0.5;
let isVibrating=false;
let timerHandle=setInterval(vibe,(1000/freq)/2);
let fd = document.getElementById("freqDisplay");
fd.text="0.5 Hz"
let decrease = document.getElementById("button-1");
  decrease.onactivate = function(evt) {
    if (freq <= 3) {
      freq=freq-0.1;
    }
    else {
      freq=freq-1;
    }
    if (freq < 0) {
      freq=0;
    }
    if (freq >= 0.1 ) {
      fd.text=freq.toFixed(1) +" Hz"
      clearInterval(timerHandle);
    timerHandle=setInterval(vibe,(1000/freq)/2);
    }
    else {
      fd.text="Rand"
      clearInterval(timerHandle);
    timerHandle=setInterval(vibe,(1000/randDelay())/2);
    }
    
    
      ;}

let increase = document.getElementById("button-2");
  increase.onactivate = function(evt) {
    if (freq < 3) {
      freq=freq+0.1;
    }
    else {
      freq=freq+1;
    }
    if (freq > 30) {
      freq=30;
    }
    fd.text=freq.toFixed(1) +" Hz"
    clearInterval(timerHandle);
    if (freq >= 30)  {
      timerHandle=setInterval(constantVibe,1000);
       fd.text="Max"
    }
    else if (freq >= 0.1) {
    timerHandle=setInterval(vibe,(1000/freq)/2);
    fd.text=freq.toFixed(1) +" Hz"
    }
     
    else {
      timerHandle=setInterval(vibe,(1000/randDelay())/2);
    }

      ;}



  