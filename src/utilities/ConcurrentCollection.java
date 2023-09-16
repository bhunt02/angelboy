package utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentCollection<E> implements Collection<E> {
    ArrayList<E> list;
    /** use this to lock for write operations like add/remove */
    private final Lock readLock;
    /** use this to lock for read operations like get/iterator/contains.. */
    private final Lock writeLock;

    {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    public ConcurrentCollection() {
        list = new ArrayList<>(10);
    }
    @Override
    public void clear() {
        writeLock.lock();
        try {
            this.list.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> e) {
        writeLock.lock();
        try {
            return this.list.removeAll(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        writeLock.lock();
        try {
            return this.list.retainAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return list.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return list.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        readLock.lock();
        boolean b = false;
        try {
            return list.contains(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        readLock.lock();
        try {
            return new ArrayList<E>( list ).iterator();
        } finally{
            readLock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        writeLock.lock();
        try {
            return list.toArray();
        } finally{
            writeLock.unlock();
        }
    }

    @Override
    public <E> E[] toArray(E[] a) {
        writeLock.lock();
        try {
            return list.toArray(a);
        } finally{
            writeLock.unlock();
        }
    }
    @Override
    public boolean add(E e) {
        writeLock.lock();
        try {
            return list.add(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object e) {
        writeLock.lock();
        try {
            return list.remove(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        readLock.lock();
        try {
            return list.containsAll(c);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> e) {
        writeLock.lock();
        try {
            return this.list.addAll(e);
        } finally {
            writeLock.unlock();
        }
    }

    public E get(int i) {
        readLock.lock();
        try {
            return this.list.get(i);
        } finally {
            readLock.unlock();
        }
    }
}
