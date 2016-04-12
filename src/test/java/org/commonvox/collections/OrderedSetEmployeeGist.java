package org.commonvox.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.commonvox.collections.KeyComponentProfile;
import org.commonvox.collections.OrderedSet;

/**
 * Simple examples of constructing and querying an OrderedSet.
 */
public class OrderedSetEmployeeGist {

  public void employeeExample() {

    KeyComponentProfile<Employee> deptComponent = new KeyComponentProfile<Employee>(Employee.class, Department.class);
    KeyComponentProfile<Employee> jobComponent = new KeyComponentProfile<Employee>(Employee.class, Job.class);

    OrderedSet<Employee> employeesByDeptAndJob
            = new OrderedSet<Employee>(deptComponent, jobComponent);
    employeesByDeptAndJob.addAll(getEmployeeList());
    printHierarchically(employeesByDeptAndJob);

    OrderedSet<Employee> employeesByJobAndDept
            = new OrderedSet<Employee>(jobComponent, deptComponent);
    employeesByJobAndDept.addAll(getEmployeeList());
    printHierarchically(employeesByJobAndDept);

    Set<Object> deptSet = employeesByJobAndDept.keyComponentSet(deptComponent);
    System.out.println("Department Listing\n===============");
    for (Object dept : deptSet) {
      System.out.println(dept);
    }
    System.out.println("===============");

    Set<Object> jobSet = employeesByJobAndDept.keyComponentSet(jobComponent);
    System.out.println("Job Listing\n===============");
    for (Object job : jobSet) {
      System.out.println(job);
    }
    System.out.println("===============");
  }

  void printHierarchically(OrderedSet<?> orderedSet) {
    final String TAB = "     ";
    List<Object> previousKeyComponents = new ArrayList<Object>(Arrays.asList("", "", ""));
    for (Entry<List<Object>,?> entry : orderedSet.entrySet()){
      List<Object> keyComponents = entry.getKey();
      Iterator<Object> keyComponentIterator = keyComponents.iterator();
      Iterator<Object> previousKeyComponentIterator = previousKeyComponents.iterator();
      int tabCount = 0;
      boolean printRemainingComponents = false;
      while (keyComponentIterator.hasNext()) {
        Object keyComponent = keyComponentIterator.next();
        if (!keyComponent.equals(previousKeyComponentIterator.next()) || printRemainingComponents) {
          printRemainingComponents = true;
          for (int i = 0; i < tabCount; i++) {
            System.out.print(TAB);
          }
          System.out.println(keyComponent);
        }
        tabCount++;
      }
      previousKeyComponents = keyComponents;
    }
    System.out.println("===============");
  }

  List<Employee> getEmployeeList() {
    return new ArrayList<Employee>(Arrays.asList(
            new Employee(1, "Smith", "Elizabeth",
                Arrays.asList(new Job("Manager"), new Job("Analyst")),
                Arrays.asList(new Department("Sales"), new Department("Accounting"), new Department("Sysops"))),
            new Employee(2, "Jones", "Alexander",
                Arrays.asList(new Job("Analyst"), new Job("TechRep")),
                Arrays.asList(new Department("Sales"), new Department("Marketing"), new Department("Accounting"))),
            new Employee(3, "Anderson", "Judith",
                Arrays.asList(new Job("ProjectLead"), new Job("Analyst"), new Job("Designer")),
                Arrays.asList(new Department("Sysops")))
            ));
  }

  public class Employee implements Comparable<Employee> {
    int id;
    String lastName, firstName;
    List<Job> jobs;
    List<Department> depts;

    public Employee(int id, String lastName, String firstName, List<Job> jobs, List<Department> depts) {
      this.id = id;
      this.lastName = lastName;
      this.firstName = firstName;
      this.jobs = jobs;
      this.depts = depts;
    }

    public List<Job> getJobs() {
      return this.jobs;
    }

    public List<Department> getDepartments() {
      return this.depts;
    }

    @Override
    public int compareTo(Employee other) {
      return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString(){
      return "EMPLOYEE #" + this.id + ": " + this.lastName + "," + this.firstName;
    }
  }

  public class Job implements Comparable<Job> {
    String jobName;

    public Job(String jobName) {
      this.jobName = jobName;
    }

    @Override
    public int compareTo(Job other) {
      return this.jobName.compareTo(other.jobName);
    }

    @Override
    public boolean equals(Object other) {
      if (!Job.class.isAssignableFrom(other.getClass())) {
        return false;
      }
      return compareTo((Job)other) == 0;
    }

    @Override
    public String toString(){
      return "JOB: " + jobName;
    }
  }

  public class Department implements Comparable<Department> {
    String deptName;

    public Department(String deptName) {
      this.deptName = deptName;
    }

    @Override
    public int compareTo(Department other) {
      return this.deptName.compareTo(other.deptName);
    }

    @Override
    public boolean equals(Object other) {
      if (!Department.class.isAssignableFrom(other.getClass())) {
        return false;
      }
      return compareTo((Department)other) == 0;
    }

    @Override
    public String toString(){
      return "DEPARTMENT: " + deptName;
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    new OrderedSetEmployeeGist().employeeExample();
  }
}
