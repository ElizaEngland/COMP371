package part1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WineInstance {

    private List<String> details;
    private int c;

    public WineInstance(List<String> details){
        this.details=details;
    }

    public List<String> getDetails() {
        return details;
    }

    public int getTrueClassOfInstance(){
        return Integer. parseInt(details.get(13));
    }

    public int getClassOfInstance(){
        return c;
    }
    public void setClass(int c){
      this.c=c;
    }




}
