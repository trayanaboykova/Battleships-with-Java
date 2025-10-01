import java.util.Scanner;

// Animal class
class Animal {
    public String makeSound() {
        // Method to return animal sound
        return "animal sound";
    }
}

// Dog class
class Dog extends Animal {
    // Write override annotation and the makeSound method here
    @Override
    public String makeSound() {
        return "bark";
    }

}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String animalType = scanner.nextLine();

        // Depending on the input create instance of Animal or Dog and call it's makeSound method
        if (animalType.equals("Animal")) {
            Animal animal = new Animal();
            System.out.println(animal.makeSound());
        } else if (animalType.equals("Dog")) {
            Dog dog = new Dog();
            System.out.println(dog.makeSound());
        }
    }
}