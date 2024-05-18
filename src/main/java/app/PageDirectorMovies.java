package app;

import java.util.ArrayList;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PageDirectorMovies implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/directormovies.html";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Head information
        html = html + "<head>" +
                "<title>Movies by Director</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";

        // Add the body
        html = html + "<body>";

        // Add the topnav
        // This uses a Java v15+ Text Block
        html = html + """
                    <div class='topnav'>
                        <a href='/'>Homepage</a>
                        <a href='movies.html'>List All Movies</a>
                        <a href='moviestype.html'>Get Movies by Type</a>
                        <a href='directormovies.html'>Get Movies by Director</a>
                    </div>
                """;

        // Add header content block
        html = html + """
                    <div class='header'>
                        <h1>
                            List Movies by Director
                        </h1>
                    </div>
                """;

        // Add Div for page Content
        html = html + "<div class='content'>";

        ArrayList<String> directors = JDBCConnection.getDirectors();

        /*
         * Add HTML for the web form
         * We are giving two ways here
         * - one for a text box
         * - one for a drop down
         * 
         * Whitespace is used to help us understand the HTML!
         * 
         * IMPORTANT! the action speicifes the URL for POST!
         */
        html = html + "<form action='/directormovies.html' method='post'>";

        html = html + "   <div class='form-group'>";
        html = html + "      <label for='director_drop'>Select the director (Dropdown):</label>";
        html = html + "      <select id='director_drop' name='director_drop'>";
        for (String dirName : directors) {
            System.out.println(dirName);
            html = html + "         <option>" + dirName + "</option>";
        }
        html = html + "      </select>";
        html = html + "   </div>";

        html = html + "   <div class='form-group'>";
        html = html + "      <label for='director_textbox'>Select the director (Textbox)</label>";
        html = html + "      <input class='form-control' id='director_textbox' name='director_textbox'>";
        html = html + "   </div>";

        html = html + "   <button type='submit' class='btn btn-primary'>Get all of the movies!</button>";

        html = html + "</form>";

        /*
         * Get the Form Data
         * from the drop down list
         * Need to be Careful!!
         * If the form is not filled in, then the form will return null!
         */
        String director_drop = context.formParam("director_drop");
        // String movietype_drop = context.queryParam("movietype_drop");
        if (director_drop == null) {
            // If NULL, nothing to show, therefore we make some "no results" HTML
            html = html + "<h2><i>No Results to show for dropbox</i></h2>";
        } else {
            // If NOT NULL, then lookup the movie by type!
            html = html + outputMovies(director_drop);
        }

        String director_textbox = context.formParam("director_textbox");
        if (director_textbox == null || director_textbox == "") {
            // If NULL, nothing to show, therefore we make some "no results" HTML
            html = html + "<h2><i>No Results to show for textbox</i></h2>";
        } else {
            // If NOT NULL, then lookup the movie by type!
            html = html + outputMovies(director_textbox);
        }

        // Close Content div
        html = html + "</div>";

        // Footer
        html = html + """
                    <div class='footer'>
                        <p>COSC2803 Module 0 - Week 06</p>
                    </div>
                """;

        // DO NOT MODIFY THIS
        // Makes Javalin render the webpage
        context.html(html);
    }

    public String outputMovies(String dirName) {
        String html = "";
        html = html + "<h2>" + dirName + " Movies</h2>";

        // Look up movies from JDBC
        ArrayList<String> movieTitles = getMoviesByDirector(dirName);

        // Add HTML for the movies list
        html = html + "<ul>";
        for (String title : movieTitles) {
            html = html + "<li>" + title + "</li>";
        }
        html = html + "</ul>";

        // 🚨 This block of code is for Pre-Req students.
        // Altneratively we can use JDBCConnection to add HTML for the movies list
        // Uncomment the code to use the JDBCConnection Objects example(s)
        JDBCConnection jdbc = new JDBCConnection();
        ArrayList<Movie> movies = jdbc.getMoviesByDirector(dirName);
        html = html + "<h2>" +
                dirName +
                " Movies with Years (from JDBCConnection)</h2>" +
                "<ul>";
        for (Movie movie : movies) {
            html = html + "<li>" + movie.name + " was made in " + movie.year + "</li>";
        }
        html = html + "</ul>";

        return html;
    }

    /**
     * Get all the movies in the database by a given type.
     * Note this takes a string of the type as an argument!
     * This has been implemented for you as an example.
     * HINT: you can use this to find all of the horror movies!
     */
    public ArrayList<String> getMoviesByDirector(String dirName) {
        ArrayList<String> movies = new ArrayList<String>();

        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = "SELECT * FROM Movie JOIN Director ON Movie.DirNumb = Director.DirNumb WHERE Director.DirName = '" + dirName + "';";
            System.out.println(query);

            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String title = results.getString("MvTitle");

                movies.add(title);
            }

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just pring the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        // Finally we return all of the movies
        return movies;
    }

}

