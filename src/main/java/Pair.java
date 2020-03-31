
public class Pair<T, T1> {
    T a ;
    T1 b ;

    public Pair() { }

    public T getKey() {
        return a ;
    }

    public T1 getValue() {
        return b ;
    }

    public Pair(T a , T1 b) {
        this.a = a ;
        this.b = b ;
    }
}
