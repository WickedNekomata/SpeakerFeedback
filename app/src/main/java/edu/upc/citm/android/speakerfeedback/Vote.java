package edu.upc.citm.android.speakerfeedback;

public class Vote {

    String pollid;
    int option;

    public Vote(String pollId, int option) {
        this.pollid = pollId;
        this.option = option;
    }

    public String getPollid() {
        return pollid;
    }

    public void setPollid(String pollid) {
        this.pollid = pollid;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
