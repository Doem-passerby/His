import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Queue{
	Lock lock = new ReentrantLock();
	Condition notFull = lock.newCondition();
	Condition notEmpty = lock.newCondition();
	
	int max = 10;
	ArrayList<Integer> arr = new ArrayList<Integer>();
	int n = 0;

	//入队
	public void enqueue(int e) {
		lock.lock();
		try {
			while (arr.size() == max) {//满
				System.out.println("Producer await,while full ");
				notFull.await();			
			}
			arr.add(e);
			notEmpty.signal();//唤醒一个  signalAll全部唤醒
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		lock.unlock();
	}
	//出队
	public int dequeue() {
		int ret = 0;
		lock.lock();
		try {
			while (arr.size() == 0) {
				System.out.println("Customer await,while empty ");
				notEmpty.await();//等待
			}
			ret = arr.remove(0);
			notFull.signal();//试图唤醒生产者
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lock.unlock();
		return ret;
	}
}
class Producer implements Runnable{
	private Queue q;
	private int id;
	public Producer(Queue q,int id){
		this.q=q;
		this.id=id;		
	}
	@Override
	public void run() {
		int idx = 0;
		try {
			while (true) {
				int e = id*100000+ idx;
				System.out.println("Producer "+ id +",put "+ e +" start");
				q.enqueue(e);
				System.out.println("Producer "+ id +",put "+ e +" succ");
				idx++;
				Thread.sleep((long)(Math.random()*1000));				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
class Customer implements Runnable{
	private Queue q;
	private int id;
	public Customer(Queue q,int id) {
		this.q=q;
		this.id=id;
	}
	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("Customer "+ id +" want");
				int r = q.dequeue();
				System.out.println("Customer "+ id +" got "+ r);
			    Thread.sleep((long)(1000*Math.random()));
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
public class ProducerAndCustomer {
	public static void main(String[] args) {
		ExecutorService executor = Executors.newCachedThreadPool();
		Queue q = new Queue();
		executor.execute(new Producer(q, 1));
		executor.execute(new Customer(q, 1));
		executor.execute(new Customer(q, 2));
		executor.execute(new Customer(q, 3));
		executor.execute(new Customer(q, 4));
		executor.shutdown();
		
	}


}
