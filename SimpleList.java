/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Peter Weston
 */
public class SimpleList {
    Object data;
    SimpleList next;

    SimpleList() {

    }

    SimpleList(Object o) {
        this.data = o;
    }

    SimpleList(Object o, SimpleList list) {
        this.data = o;
        this.next = list;
    }

    public void add(Object o) {
        if(this.next == null)
            this.next = new SimpleList(o);
        else
            this.next.add(o);
    }

    public void add(SimpleList list) {
        if(this.next == null)
            this.next = list;
        else
            this.next.add(list);
    }

    public void print() {
        SimpleList temp;
        temp = this;
        do {
            System.out.println(temp.data);
            if(temp.next == null)
                break;
            temp = temp.next;
        } while(true);
    }
}
