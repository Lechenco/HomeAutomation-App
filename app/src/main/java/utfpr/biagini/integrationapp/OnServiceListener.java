package utfpr.biagini.integrationapp;

public interface OnServiceListener {
    public abstract void isConnected(boolean status);

    public abstract void getMessage(String field, int message);
}
