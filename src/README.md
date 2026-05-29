# CS4700-Project_2_FTP_Client

### GitHub Page: https://github.com/MicahMarshall242/CS4700-Project_2_FTP_Client


## High Level Approach 
in beginning this project, I didn't have as easy of a time figuring out how to structure my
code and place responsibilities. I started with what I knew, though. I found a perfect URL/URI
parsing library and handled command line args. 

I also was quick to create the FTPConnection class, as I knew that was a critical component
for this project. The SocketWrapper class came in handy as well, as I didn't want to directly
manage two ports and four streams of data.  I also didn't need all the raw features provided
by java sockets, so taking this action was nice.

Once I established a connection to the server and got comfortable with the project specs was
when I had the most fun.


## Challenges

I faced many small challenges this project, none of them being too bothersome. My biggest challenge
was structuring the code and separating class/function responsibilities. I decided to continue and complete
the project first before prematurely optimizing and refactoring my code. Towards the end, I restructured 
the code to be a lot cleaner, reusable, and modular. (reflected in commit history) It's good to get something
working first, then go back and clean it up.

## Testing

Although they won't be present in my submission nor on GitHub, I wrote shell script executables
to sweep different command combinations to see whether my code works. 

- download 2 files
- upload 2 files
- remove files + dir

These files together provided a good benchmark for me to see whether my code was correct or
not – along with the autograder.
