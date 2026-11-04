package be.kdg.airfreight.common.messages;

public enum MessageKind {
    FLM, MVT_ED, MVT_EA, MVT_AD, MVT_AA, CPM;
    public boolean isTelegram() { return name().startsWith("MVT_"); }
}
