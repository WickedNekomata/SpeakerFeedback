package edu.upc.citm.android.speakerfeedback;

public class Vote {

    String pollId;
    int option;

    public Vote(String pollId, int option) {
        this.pollId = pollId;
        this.option = option;
    }

    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
