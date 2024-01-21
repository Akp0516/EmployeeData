import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmployeeAnalysis {

     public static void main(String[] args) {

          String filePath = "C:/Users/kumar/OneDrive/Desktop/Sheet1.csv";

          try {
               // Read the file and perform analysis
               analyzeEmployeeData(filePath);
          } catch (IOException | ParseException e) {
               e.printStackTrace();
          }
     }

     private static void analyzeEmployeeData(String filePath) throws IOException, ParseException {

          BufferedReader reader = new BufferedReader(new FileReader(filePath));
          String line;

          reader.readLine();

          List<EmployeeInfo> consecutiveDaysList = new ArrayList<>();
          List<EmployeeInfo> shortBreaksList = new ArrayList<>();
          List<EmployeeInfo> moreThan14HoursList = new ArrayList<>();

          Map<String, Integer> consecutiveDaysMap = new HashMap<>();

          Map<String, List<Shift>> employeeShiftsMap = new HashMap<>();

          // Parse each line and store shifts for each employee
          while ((line = reader.readLine()) != null) {
               System.out.println("Processing line: " + line);
               String[] columns = line.split(",");
               if (columns.length >= 8) {
                    String employeeName = columns[7];
                    String position = columns[1];
                    String startTimeStr = columns[2];
                    String endTimeStr = columns[3];

                    // Check if date values are empty or blank
                    if (startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
                         System.out.println("Skipping line due to empty date values: " + line);
                         continue;
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

                    Date startTime = dateFormat.parse(startTimeStr);
                    Date endTime = dateFormat.parse(endTimeStr);

                    Shift shift = new Shift(position, startTime, endTime);

                    // Check conditions and add to respective lists
                    checkConsecutiveDays(consecutiveDaysMap, employeeName, startTime);
                    addShift(employeeShiftsMap, employeeName, shift);

                    if (hasShortBreaks(employeeShiftsMap.get(employeeName), 1, 10)) {
                         shortBreaksList.add(new EmployeeInfo(employeeName, position));
                    }

                    if (hasLongShifts(employeeShiftsMap.get(employeeName), 14)) {
                         moreThan14HoursList.add(new EmployeeInfo(employeeName, position));
                    }
               }
          }

          // Check employees with consecutive days
          for (Map.Entry<String, Integer> entry : consecutiveDaysMap.entrySet()) {
               if (entry.getValue() >= 7) {
                    String[] parts = entry.getKey().split("_");
                    String employeeName = parts[0];
                    String position = parts[1];
                    consecutiveDaysList.add(new EmployeeInfo(employeeName, position));
               }
          }

          printEmployees("Employees who have worked for 7 consecutive days:", consecutiveDaysList);
          printEmployees("Employees who have less than 10 hours between shifts but greater than 1 hour:",
                    shortBreaksList);
          printEmployees("Employees who have worked for more than 14 hours in a single shift:", moreThan14HoursList);

          reader.close();
     }

     private static void printEmployees(String message, List<EmployeeInfo> employees) {
          if (!employees.isEmpty()) {
               System.out.println(message);
               for (EmployeeInfo employee : employees) {
                    System.out.println(
                              "Employee \"" + employee.getName() + "\" (Position: " + employee.getPosition() + ")");
               }
               System.out.println();
          }
     }

     private static void checkConsecutiveDays(Map<String, Integer> consecutiveDaysMap, String employeeName,
               Date startTime) {
          // Check consecutive days worked
          Date currentDate = removeTimeFromDate(startTime);
          String key = employeeName + "_" + currentDate.getTime();

          if (consecutiveDaysMap.containsKey(key)) {
               consecutiveDaysMap.put(key, consecutiveDaysMap.get(key) + 1);
          } else {
               consecutiveDaysMap.put(key, 1);
          }
     }

     private static void addShift(Map<String, List<Shift>> employeeShiftsMap, String employeeName, Shift shift) {

          if (employeeShiftsMap.containsKey(employeeName)) {
               employeeShiftsMap.get(employeeName).add(shift);
          } else {
               List<Shift> shifts = new ArrayList<>();
               shifts.add(shift);
               employeeShiftsMap.put(employeeName, shifts);
          }
     }

     private static boolean hasShortBreaks(List<Shift> shifts, int minBreakHours, int maxBreakHours) {

          Collections.sort(shifts, Comparator.comparing(Shift::getStartTime));

          for (int i = 1; i < shifts.size(); i++) {
               long diffInHours = (shifts.get(i).getStartTime().getTime() - shifts.get(i - 1).getEndTime().getTime())
                         / (60 * 60 * 1000);
               if (diffInHours > minBreakHours && diffInHours < maxBreakHours) {
                    return true;
               }
          }
          return false;
     }

     private static boolean hasLongShifts(List<Shift> shifts, int maxShiftHours) {
          // Check for shifts longer than the specified hours
          for (Shift shift : shifts) {
               long shiftDuration = (shift.getEndTime().getTime() - shift.getStartTime().getTime())
                         / (60 * 60 * 1000);
               if (shiftDuration > maxShiftHours) {
                    return true;
               }
          }
          return false;
     }

     private static Date removeTimeFromDate(Date date) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.set(Calendar.HOUR_OF_DAY, 0);
          calendar.set(Calendar.MINUTE, 0);
          calendar.set(Calendar.SECOND, 0);
          calendar.set(Calendar.MILLISECOND, 0);
          return calendar.getTime();
     }

     static class Shift {
          String position;
          Date startTime;
          Date endTime;

          Shift(String position, Date startTime, Date endTime) {
               this.position = position;
               this.startTime = startTime;
               this.endTime = endTime;
          }

          public Date getStartTime() {
               return startTime;
          }

          public Date getEndTime() {
               return endTime;
          }
     }

     static class EmployeeInfo {
          String name;
          String position;

          EmployeeInfo(String name, String position) {
               this.name = name;
               this.position = position;
          }

          public String getName() {
               return name;
          }

          public String getPosition() {
               return position;
          }
     }
}
