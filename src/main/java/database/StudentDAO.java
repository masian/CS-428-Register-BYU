package database;

import com.mongodb.DB;
import com.mongodb.WriteResult;
import exceptions.DatabaseException;
import models.Section;
import models.Student;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import packages.Courses;

/**
 * Created by sean on 2/17/15.
 */
public class StudentDAO implements IStudentDAO
{
    private DB Db;
    private MongoCollection students;
    private final String studentIdQuery = "{studentId: #}";

    public StudentDAO(DB Db)
    {
        this.Db = Db;
        Jongo jongo = new Jongo(this.Db);
        this.students = jongo.getCollection("students");
    }

    /**
     * Add one student to the database
     * @param student
     */
    @Override
    public void addStudent(Student student)
    {
        WriteResult result = this.students.insert(student);
    }


    /**
     * Delete one student from the database
     * @param student
     */
    @Override
    public void deleteStudent(Student student)
    {
        WriteResult result = this.students.remove(studentIdQuery, student.getStudentId());
        DBValidator.validate(result);
    }

    /**
     * Add a section to the database
     * @param section
     */
    @Override
    public void addSection(Section section, Student student)
    {

    }

    /**
     * Remove a section that
     * @param section
     */
    @Override
    public void removeSection(Section section,Student student)
    {

    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public Student getStudent(String id)
    {
        Student student = this.students.findOne(studentIdQuery, id).as(Student.class);
        if(student == null)
        {
            throw new DatabaseException("Could not find student");
        }

        return student;
    }

    /**
     * Add a list of courses to the database
     * @param course
     */
    @Override
    public void addCourses(Courses course)
    {

    }

    @Override
    public void removeCourses(Courses course)
    {

    }

}
