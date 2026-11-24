package com.naatalvote.domain.common;

/** Une opération valide dans sa forme contredit l'état persistant du scrutin. */
public final class ConflictException extends DomainException {
  public ConflictException(String message) { super(message); }
}
