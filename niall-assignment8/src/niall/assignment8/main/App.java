package niall.assignment8.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class App {

	private List<Integer> numbersList = Collections.synchronizedList(new ArrayList<>());

	public static void main(String[] args) {
		new App().execute();
	}

	private void execute() {
		getData();
		synchronized (numbersList) {
			Integer totalNumbers = numbersList.size();
			if (totalNumbers == 1000000) {
				System.out.println("Data parsing complete!\n");
				outputNumberFrequency(numbersList);
			} else {
				System.out.println("Data parsing error!");
			}
		}
	}

	private void getData() {
		Assignment8 assignment = new Assignment8();

		List<CompletableFuture<Void>> tasks = new ArrayList<>();
		CompletableFuture<Void> task;

		ExecutorService service = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			synchronized (numbersList) {
				task = CompletableFuture.supplyAsync(() -> assignment.getNumbers(), service)
						.thenAcceptAsync(nums -> numbersList.addAll(nums), service);
			}
			tasks.add(task);
		}

		while (tasks.stream()
				.filter(CompletableFuture::isDone)
				.count() < 1000) {
		}

		service.shutdown();
	}

	private void outputNumberFrequency(List<Integer> numbersList) {

		synchronized (numbersList) {
			Map<Object, Long> numberCounts = numbersList.stream()
					.collect(Collectors.groupingBy(number -> number, Collectors.counting()));

			System.out.println("Frequency of each number: ");
			for (Object key : numberCounts.keySet()) {
				System.out.println(key + "=" + numberCounts.get(key));
			}
		}
	}
}
