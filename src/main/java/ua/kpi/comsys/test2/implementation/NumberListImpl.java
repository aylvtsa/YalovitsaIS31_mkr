/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import ua.kpi.comsys.test2.NumberList;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * Custom implementation of INumberList interface.
 * Has to be implemented by each student independently.
 *
 * Реалізація для варіанту 28:
 * - тип списку: кільцевий однонаправлений;
 * - основна система числення: десяткова (base 10);
 * - додаткова система числення: шістнадцяткова (base 16);
 * - додаткова операція: додавання двох чисел.
 *
 * @author Яловіца Анастасія Ігорівна, ІС-31, варіант 28
 */
public class NumberListImpl implements NumberList {

    /** Основа для основної системи числення (десяткова). */
    private static final int MAIN_BASE = 10;

    /** Основа для додаткової системи числення (шістнадцяткова). */
    private static final int EXTRA_BASE = 16;

    /**
     * Внутрішній вузол кільцевого однонаправленого списку.
     * Кожен вузол відповідає одній цифрі числа.
     */
    private static class Node {
        byte value;
        Node next;

        Node(byte value) {
            this.value = value;
        }
    }

    /** Посилання на старший розряд (голову списку). */
    private Node head;

    /** Посилання на молодший розряд (хвіст списку). */
    private Node tail;

    /** Кількість елементів у списку. */
    private int size;

    /** Основа системи числення для поточного списку. */
    private final int base;

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this.base = MAIN_BASE;
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this.base = MAIN_BASE;
        StringBuilder sb = new StringBuilder();
        if (file == null) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (IOException e) {
            // при помилці читання залишаємо список порожнім, лише лог пишемо
            System.err.println("Cannot read file: " + e.getMessage());
            return;
        }
        initFromDecimalString(sb.toString());
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this.base = MAIN_BASE;
        initFromDecimalString(value);
    }

    /**
     * Допоміжна ініціалізація зі звичайного десяткового рядка.
     * Якщо рядок порожній або містить нецифрові символи — список лишається порожнім.
     */
    private void initFromDecimalString(String decimal) {
        if (decimal == null) {
            return;
        }
        decimal = decimal.trim();
        if (decimal.isEmpty() || !decimal.matches("\\d+")) {
            return;
        }
        for (int i = 0; i < decimal.length(); i++) {
            char ch = decimal.charAt(i);
            byte digit = (byte) (ch - '0');
            add(digit);
        }
    }


    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(toDecimalString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file", e);
        }
    }


    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        // тут можна повернути реальний номер залікової за умовою завдання
        return 28;
    }

    /**
     * Перетворює поточне число у BigInteger з урахуванням основи base.
     */
    private BigInteger toBigInteger() {
        BigInteger result = BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(base);

        Node cur = head;
        for (int i = 0; i < size; i++) {
            int digit = cur.value & 0xFF;
            result = result.multiply(b).add(BigInteger.valueOf(digit));
            cur = cur.next;
        }
        return result;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in other scale of notation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * @return <tt>NumberListImpl</tt> in other scale of notation.
     */
    public NumberListImpl changeScale() {
        BigInteger value = toBigInteger();
        return new NumberListImpl(value, EXTRA_BASE);
    }

    /**
     * Приватний конструктор: будує список із BigInteger у заданій основі.
     * Використовується для конвертацій між системами числення.
     */
    private NumberListImpl(BigInteger value, int base) {
        this.base = base;
        if (value.signum() == 0) {
            add((byte) 0);
            return;
        }
        BigInteger b = BigInteger.valueOf(base);
        BigInteger tmp = value;

        // збираємо цифри у зворотному порядку (молодший → старший)
        List<Byte> digitsReversed = new ArrayList<>();
        while (tmp.signum() > 0) {
            BigInteger[] dr = tmp.divideAndRemainder(b);
            tmp = dr[0];
            digitsReversed.add(dr[1].byteValue());
        }
        // додаємо так, щоб старший розряд став першим у списку
        for (int i = digitsReversed.size() - 1; i >= 0; i--) {
            add(digitsReversed.get(i));
        }
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * additional operation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * @param arg - second argument of additional operation
     *
     * @return result of additional operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (!(arg instanceof NumberListImpl)) {
            throw new IllegalArgumentException("Argument must be NumberListImpl");
        }
        NumberListImpl other = (NumberListImpl) arg;
        BigInteger a = this.toBigInteger();
        BigInteger b = other.toBigInteger();
        BigInteger sum = a.add(b);
        return new NumberListImpl(sum, MAIN_BASE);
    }


    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        BigInteger value = toBigInteger();
        return value.toString(10);
    }


    @Override
    public String toString() {
        // Рядок у поточній системі числення (base).
        // Для base = 16 цифри 10..15 подаються як A..F.
        if (size == 0) return "";
        StringBuilder sb = new StringBuilder();
        Node cur = head;
        for (int i = 0; i < size; i++) {
            int d = cur.value & 0xFF;
            if (base == 16 && d >= 10) {
                sb.append((char) ('A' + (d - 10)));
            } else {
                sb.append(d);
            }
            cur = cur.next;
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        // Порівняння двох списків за основою, розміром і послідовністю цифр.
        if (this == o) return true;
        if (!(o instanceof NumberListImpl)) return false;
        NumberListImpl other = (NumberListImpl) o;
        if (this.base != other.base || this.size != other.size) return false;

        Node c1 = this.head;
        Node c2 = other.head;
        for (int i = 0; i < size; i++) {
            if (c1.value != c2.value) return false;
            c1 = c1.next;
            c2 = c2.next;
        }
        return true;
    }


    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Додає елемент у кінець кільцевого списку (оновлює head/tail).
     */
    private void linkLast(byte value) {
        Node n = new Node(value);
        if (size == 0) {
            head = tail = n;
            n.next = n; 
        } else {
            tail.next = n;
            n.next = head;
            tail = n;
        }
        size++;
    }


    @Override
    public boolean add(Byte e) {
        Objects.requireNonNull(e);
        if (e < 0 || e >= base) {
            throw new IllegalArgumentException("Digit out of range for base " + base);
        }
        linkLast(e);
        return true;
    }


    @Override
    public void clear() {
        head = tail = null;
        size = 0;
    }

    /**
     * Повертає вузол за індексом (0..size-1).
     */
    private Node node(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        Node cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
        }
        return cur;
    }


    @Override
    public Byte get(int index) {
        return node(index).value;
    }


    @Override
    public Byte set(int index, Byte element) {
        Objects.requireNonNull(element);
        if (element < 0 || element >= base)
            throw new IllegalArgumentException();
        Node n = node(index);
        byte old = n.value;
        n.value = element;
        return old;
    }


    @Override
    public void add(int index, Byte element) {
        Objects.requireNonNull(element);
        if (element < 0 || element >= base)
            throw new IllegalArgumentException();
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();

        if (index == size) {
            add(element);
            return;
        }

        Node newNode = new Node(element);
        if (index == 0) {
            if (size == 0) {
                head = tail = newNode;
                newNode.next = newNode;
            } else {
                newNode.next = head;
                head = newNode;
                tail.next = head;
            }
        } else {
            Node prev = node(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;
        }
        size++;
    }


    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        Node removed;
        if (size == 1) {
            removed = head;
            clear();
            return removed.value;
        }
        if (index == 0) {
            removed = head;
            head = head.next;
            tail.next = head;
        } else {
            Node prev = node(index - 1);
            removed = prev.next;
            prev.next = removed.next;
            if (removed == tail) {
                tail = prev;
            }
        }
        size--;
        return removed.value;
    }


    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }


    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte v = (Byte) o;
        Node cur = head;
        for (int i = 0; i < size; i++) {
            if (cur.value == v) return i;
            cur = cur.next;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte v = (Byte) o;
        int idx = -1;
        Node cur = head;
        for (int i = 0; i < size; i++) {
            if (cur.value == v) idx = i;
            cur = cur.next;
        }
        return idx;
    }


    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node cur = head;
            private int passed = 0;

            @Override
            public boolean hasNext() {
                return passed < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) throw new NoSuchElementException();
                byte v = cur.value;
                cur = cur.next;
                passed++;
                return v;
            }
        };
    }


    @Override
    public Object[] toArray() {
        Byte[] arr = new Byte[size];
        int i = 0;
        for (Byte b : this) {
            arr[i++] = b;
        }
        return arr;
    }


    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
        int idx = indexOf(o);
        if (idx == -1) return false;
        remove(idx);
        return true;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if (!contains(o)) return false;
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean changed = false;
        for (Byte b : c) changed |= add(b);
        return changed;
    }


    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        int i = index;
        for (Byte b : c) {
            add(i++, b);
        }
        return !c.isEmpty();
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            while (remove(o)) changed = true;
        }
        return changed;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<Byte> it = iterator();
        int idx = 0;
        while (it.hasNext()) {
            Byte b = it.next();
            if (!c.contains(b)) {
                remove(idx);
                changed = true;
                idx--;
            }
            idx++;
        }
        return changed;
    }


    @Override
    public ListIterator<Byte> listIterator() {
        return listIterator(0);
    }
    

    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
    
        return new ListIterator<Byte>() {
            private int cursor = index;          // позиція наступного елемента
            private int lastReturned = -1;       // індекс останнього поверненого елемента, або -1
    
            @Override
            public boolean hasNext() {
                return cursor < size;
            }
    
            @Override
            public Byte next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Byte value = get(cursor);
                lastReturned = cursor;
                cursor++;
                return value;
            }
    
            @Override
            public boolean hasPrevious() {
                return cursor > 0;
            }
    
            @Override
            public Byte previous() {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                }
                cursor--;
                Byte value = get(cursor);
                lastReturned = cursor;
                return value;
            }
    
            @Override
            public int nextIndex() {
                return cursor;
            }
    
            @Override
            public int previousIndex() {
                return cursor - 1;
            }
    
            @Override
            public void remove() {
                if (lastReturned < 0) {
                    throw new IllegalStateException();
                }
                NumberListImpl.this.remove(lastReturned);
                if (lastReturned < cursor) {
                    cursor--;
                }
                lastReturned = -1;
            }
    
            @Override
            public void set(Byte b) {
                if (lastReturned < 0) {
                    throw new IllegalStateException();
                }
                NumberListImpl.this.set(lastReturned, b);
            }
    
            @Override
            public void add(Byte b) {
                NumberListImpl.this.add(cursor, b);
                cursor++;
                lastReturned = -1;
            }
        };
    }
    


    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        List<Byte> result = new ArrayList<>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            result.add(get(i));
        }
        return result;
    }
    

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            return false;
        }
        if (index1 == index2) return true;
        Node n1 = node(index1);
        Node n2 = node(index2);
        byte tmp = n1.value;
        n1.value = n2.value;
        n2.value = tmp;
        return true;
    }


    /**
     * Сортування за зростанням (bubble sort по значеннях вузлів).
     */
    @Override
    public void sortAscending() {
        if (size < 2) return;
        for (int i = 0; i < size - 1; i++) {
            Node cur = head;
            for (int j = 0; j < size - 1; j++) {
                Node next = cur.next;
                if ((cur.value & 0xFF) > (next.value & 0xFF)) {
                    byte tmp = cur.value;
                    cur.value = next.value;
                    next.value = tmp;
                }
                cur = cur.next;
            }
        }
    }


    /**
     * Сортування за спаданням (через sortAscending + розворот списку).
     */
    @Override
    public void sortDescending() {
        sortAscending();
        int left = 0;
        int right = size - 1;
        while (left < right) {
            swap(left, right);
            left++;
            right--;
        }
    }


    /**
     * Циклічний зсув вліво: 1 2 3 4 → 2 3 4 1.
     */
    @Override
    public void shiftLeft() {
        if (size <= 1) return;
        head = head.next;
        tail = tail.next;
    }


    /**
     * Циклічний зсув вправо: 1 2 3 4 → 4 1 2 3.
     */
    @Override
    public void shiftRight() {
        if (size <= 1) return;
        Node prev = node(size - 2);
        head = tail;
        tail = prev;
        tail.next = head;
    }
}
