import org.w3c.dom.ls.LSOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static final double kMulX = 7.0843;
    public static final double kMulY = 7.0855;
    public static void main(String[] args) {

        // todo замени путь к файлу
        String filePath = "C:/Users/titko/OneDrive/Рабочий стол/camera_block.eps";

        List<List<List<Integer>>> listOfFigures = getFromEps(filePath);
        ShowList(listOfFigures);
        System.out.println("----------------");
        addFirstElementAsLast(listOfFigures);
        ShowList(listOfFigures);
        System.out.println("========================");
        System.out.println("===========================");
        System.out.println("========================");
        rotate(listOfFigures);
        ShowList(listOfFigures);
        sortFigures(listOfFigures);
        ShowList(listOfFigures);

    }

    public static void ShowList (List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> listOfFigure : listOfFigures) {
            System.out.println(listOfFigure);
        }
    }

    public static List<List<List<Integer>>> getFromEps(String filePath){
        List<List<List<Integer>>> listOfFigures = new ArrayList<>();

        List<String> blocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean reachedEndData = false;
            boolean reachedBeginData = false;
            boolean blockStarted = false;

            while ((line = reader.readLine()) != null) {
                if (!reachedBeginData) {
                    if (line.trim().startsWith("%%EndPageSetup")) {
                        reachedBeginData = true;
                    }
                } else if (!reachedEndData) {

                    if (line.startsWith("%ADO")) {
                        reachedEndData = true;
                    } else {
                        if(line.contains("mo") && Character.isDigit(line.charAt(0))){
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.contains("m") && Character.isDigit(line.charAt(0))) {
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.trim().equals("cp") || line.trim().equals("@c") || line.trim().equals("@")) {
                            blockStarted = false;
                        } else if (blockStarted) {
                            blocks.add(line);
                        }
                    }
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading EPS file: " + e.getMessage());
            return new ArrayList<>();
        }

        int current_figure = -1;
        for (String block : blocks) {
            String[] line_parts = block.split(" ");

            if (Objects.equals(line_parts[line_parts.length - 1], "mo") || Objects.equals(line_parts[line_parts.length - 1], "m")) {
                List<Integer> listN = new ArrayList<>();
                current_figure++;
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "li")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "cv") || Objects.equals(line_parts[line_parts.length - 1], "C")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            }
        }

        removeEmptyLists(listOfFigures);
        removeNotCycledFigures(listOfFigures);

        return listOfFigures;
    }

    public static void removeEmptyLists(List<List<List<Integer>>> listOfFigures) {
        listOfFigures.removeIf(List::isEmpty);
    }
    public static void removeNotCycledFigures(List<List<List<Integer>>> listOfFigures) {
        Iterator<List<List<Integer>>> iterator = listOfFigures.iterator();
        while (iterator.hasNext()) {
            List<List<Integer>> listOfFigure = iterator.next();
            int last_x = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 2);
            int last_y = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 1);
            int first_x = listOfFigure.get(0).get(0);
            int first_y = listOfFigure.get(0).get(1);
            if (first_x != last_x || first_y != last_y) {
                iterator.remove();
            }
        }
    }

    private static void getNumericalWithDot(int current_figure, List<List<List<Integer>>> listOfFigures, String[] line_parts, List<Integer> listN) {
        for (int j = 0; j < line_parts.length - 1; j++) {
            if (line_parts[j].startsWith(".")) {
                line_parts[j] = "0" + line_parts[j];
            }
            double calk;
            if (j % 2 != 0) {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulX;
            } else {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulY;
            }
            int this_int = (int) Math.round(calk);
            listN.add(this_int);
        }
        listOfFigures.get(current_figure).add(listN);
    }
    public static void addFirstElementAsLast(List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> listOfFigure : listOfFigures) {
            if(!listOfFigure.isEmpty()) {
                List<Integer> first = new ArrayList<>(listOfFigure.getFirst());
                listOfFigure.add(first);
            }
        }
    }
    public static void sortFigures(List<List<List<Integer>>> listOfFigures){
        for (List<List<Integer>> figure : listOfFigures) {
            figure.sort(Comparator.comparingInt(List::size));
        }
        listOfFigures.sort(Comparator.comparingInt(List::size));
    }

    public static void rotate(List<List<List<Integer>>> listOfFigures){
        for (List<List<Integer>> figure : listOfFigures) {
            for(List<Integer> list: figure){
                int count = 0;
                int temp = 0;
                int size = list.size();
                for (int i = 0; i < size / 2; i++) {
                     temp = list.get(i);
                    list.set(i, list.get(size - 1 - i));
                    list.set(size - 1 - i, temp);
                }
            }
        }
    }
}