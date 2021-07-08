package il.ac.openu.flue.yogi;

interface RE {
//  static interface Then extends RE { }
//  static interface Or extends RE { }
//  static interface Star extends RE { }
//  static interface Char extends RE { char c(); }
//  <T> T reduce(Reducer<T> r);
//
//  interface Reducer<T> {T character(Char c); T then(T t1, T t2); T or(T t1, T t2); T star(T t); }
//  static RE c(char c) { return new Char() { @Override public char c(){ return c; } @Override public <T> T reduce(Reducer<T> r) { return r.character(this); } }; }
//  default Star star() { return new Star() { @Override public <T> T reduce(Reducer<T> r) { return r.star(RE.this.reduce(r)); } }; }
//  default Then then(RE x) { return new Then() { @Override public <T> T reduce(Reducer<T> r) { return r.then(RE.this.reduce(r), x.reduce(r)); } }; }
//  default Or or(RE x) { return new Or() { @Override public <T> T reduce(Reducer<T> r) { return r.or(RE.this.reduce(r), x.reduce(r)); } ;}; }
//  default RE clone() {
//    return reduce(new Reducer<RE>() {
//      @Override public RE star(RE r) { return r.star(); }
//      @Override public RE character(Char c) { return RE.c(RE.this.c()); }
//      @Override public RE then(RE r1, RE r2) { return r1.then(r2); }
//      @Override public RE or(RE o1, RE o2) { return o1.or(o2); }
//    });
//  }
//  public static void main (String[]args){
//    RE.c('a').or(RE.c('b'));
//  }
}
