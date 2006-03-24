public class Clock {
  //@ public model long _time;                                    
  //@ private represents _time = second + minute*60 + hour*60*60;

  //@ private invariant 0 <= hour && hour <= 23;
  private int hour; //@ in _time;

  //@ private invariant 0 <= minute && minute <= 59;
  private int minute; //@ in _time;

  //@ private invariant 0 <= second && second <= 59;
  private int second; //@ in _time;

  //@   ensures getHour() == 12 && getMinute() == 0 && getSecond() == 0;
  public /*@ pure @*/ Clock() {
    hour = 12; minute = 0; second = 0;
  }

  //@ ensures 0 <= \result && \result <= 23;
  public /*@ pure @*/ int getHour() { return hour; }

  //@ ensures 0 <= \result && \result <= 59;
  public /*@ pure @*/ int getMinute() { return minute; }

  //@ ensures 0 <= \result && \result <= 59;
  public /*@ pure @*/ int getSecond() { return second; }

  /*@ requires 0 <= hour && hour <= 23;
    @ requires 0 <= minute && minute <= 59;
    @ assignable _time;
    @ ensures getHour() == hour && getMinute() == minute && getSecond() == 0;
    @*/
  public void setTime(int hour, int minute) {
    this.hour = hour; this.minute = minute; this.second = 0;
  }

  //@ assignable _time;
  //@ ensures _time == \old(_time + 1) % 60*60*24;        
  public void tick() {
    second++;
    if (second == 60) { second = 0; minute++; }
    if (minute == 60) { minute = 0; hour++; }
    if (hour == 24)   { hour = 0; }
  }
}