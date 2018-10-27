package org.mozartoz.truffle.runtime;

public class OzPort extends OzValue {

	private OzFuture stream;

	public OzPort(OzVar streamVar) {
		this.stream = new OzFuture();
		streamVar.link(stream);
	}

	public void send(Object value) {
		OzFuture newTail = new OzFuture();
		OzCons cons = new OzCons(value, newTail);
		((OzFuture) stream).bind(cons);
		stream = newTail;
	}

	@Override
	public String toString() {
		return "<Port>";
	}

}
