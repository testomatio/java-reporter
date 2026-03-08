package io.testomat.karate.adapter;

public interface KarateEngineAdapter {

    Object getVariable(String name);

    void setVariable(String name, Object value);
}
