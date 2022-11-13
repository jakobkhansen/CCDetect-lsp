package TestJavaCodebase;

/**
 * Huge
 */
public class LinkedList<T> {

    private Node forste;
    private int antall;

    public LinkedList() {
        this.forste = null;
        this.antall = 0;
    }

    private class Node {

        private T innhold;
        private Node neste;

        public Node(T innhold) {
            this.innhold = innhold;
            neste = null;
        }
    }

    public int stoerrelse() {
        return antall;
    }

    public void leggTil(int pos, T x) {
        Node nyNode = new Node(x);

        if (pos == antall) {
            this.leggTil(x);
            return;
        }

        else if (pos == 0) {
            nyNode.neste = forste;
            forste = nyNode;
        }

        else if (pos < 0) {
            throw new RuntimeException();
        }

        else {
            Node loop = forste;
            Node forrige = null;
            try {
                for (int i = 0; i < pos; i++) {
                    forrige = loop;
                    loop = loop.neste;
                }
            } catch (NullPointerException e) {
            }

            nyNode.neste = loop;
            forrige.neste = nyNode;
        }
        antall++;
    }

    public void leggTil(T x) {

        Node nyNode = new Node(x);

        if (antall == 0) {
            forste = nyNode;
        } else {
            Node loop = forste;
            while (loop.neste != null) {
                loop = loop.neste;
            }

            loop.neste = nyNode;
        }
        antall++;
    }

    public void sett(int pos, T x) {

        if (pos < 0) {
            throw new RuntimeException();
        } else {
            Node loop = forste;
            try {
                for (int i = 0; i < pos; i++) {
                    loop = loop.neste;
                }
                loop.innhold = x;
            } catch (NullPointerException e) {
                throw new RuntimeException();
            }
        }
    }

    public T hent(int pos) {

        if (pos < 0) {
            throw new RuntimeException();
        }

        Node loop = forste;
        try {
            for (int i = 0; i < pos; i++) {
                loop = loop.neste;

            }

            return loop.innhold;

        }

        catch (NullPointerException e) {
            throw new RuntimeException();
        }
    }

    // Partly clone of LinkedList.fjern()
    public T fjern(int pos) {

        Node loop = forste;
        Node forrige = null;

        if (antall == 0) {
            System.out.println("her");
            throw new RuntimeException();
        }

        else if (pos == 0) {
            Node temp = forste;
            if (antall == 0) {
                throw new RuntimeException();
            } else if (antall == 1) {
                forste = null;
            } else {
                forste = forste.neste;
            }
            antall--;
            return temp.innhold;
        }

        else {
            try {
                for (int i = 0; i < pos; i++) {
                    forrige = loop;
                    loop = loop.neste;
                }
                forrige.neste = loop.neste;
                antall--;
                return loop.innhold;

            } catch (NullPointerException e) {
                throw new RuntimeException();
            }

        }

    }

    // Partly clone of LinkedList.fjern(int pos)
    public T fjern() {

        Node temp = forste;
        if (antall == 0) {
            throw new RuntimeException();
        } else if (antall == 1) {
            forste = null;
        } else {
            forste = forste.neste;
        }
        antall--;
        return temp.innhold;
    }
}
