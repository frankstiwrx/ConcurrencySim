package com.frankstiwrx.concurrencysim.sync;

public enum LockType {
    SPINLOCK("Spinlock (Busy Waiting)"),
    MUTEX("Mutex (Bloqueio Passivo)");

    private final String descricao;

    LockType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}