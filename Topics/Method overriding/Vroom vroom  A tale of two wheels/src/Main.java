import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int speed = scanner.nextInt();
        scanner.close();

        Vehicle car = new Car(speed);
        Vehicle motorcycle = new Motorcycle(speed);

        System.out.println(car.getInfo());
        System.out.println(motorcycle.getInfo());
    }
}

class Vehicle {
    protected int speed;

    public Vehicle(int speed) {
        this.speed = speed;
    }

    public String getInfo() {
        return "Vehicle: Speed " + speed + " mph";
    }
}

class Car extends Vehicle {
    private int doors = 4; // Default: 4 doors

    public Car(int speed) {
        super(speed);
    }

    @Override
    public String getInfo() {
        return "Car: Speed " + speed + " mph, Doors: " + doors;
    }
}

class Motorcycle extends Vehicle {
    private boolean hasSidecar = false; // Default: no sidecar

    public Motorcycle(int speed) {
        super(speed);
    }

    @Override
    public String getInfo() {
        return "Motorcycle: Speed " + speed + " mph, Sidecar: " + hasSidecar;
    }
}
