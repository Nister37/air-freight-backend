package be.kdg.airfreight.partnermessaging.domain;

public enum MessageKind {
    FLM, MVT_ED, MVT_EA, MVT_AD, MVT_AA, CPM;
    public String routingKey() { return name().toLowerCase().replace('_', '.'); }
    public boolean isTelegram() { return name().startsWith("MVT_"); }
}
