import java.io.*;
import java.util.StringTokenizer;

class FileSystem {
      
      static final int MAX_LEVELS = 50; 
      static String[] currentWorkingDirectory;
      static int numLevels; 
      static Inode[] inodePath;
      static Inode currentWorkingDirectoryInode;
      static DirectoryBlock[] directoryBlockPath;
      static DirectoryBlock currentWorkingDirectoryDirNode;
      
      public static void main(String[] args)
      {
         String commandLine;
         
         try {
           currentWorkingDirectory = new String[MAX_LEVELS];
           numLevels = 0;
           System.out.println("Type help to find out the available commands");
           Block.init(); // Initializes file system and creates the root directory
           currentWorkingDirectoryInode = Block. rootInode;
           currentWorkingDirectoryDirNode = Block.rootDirectoryBlock; 
           inodePath = new Inode[MAX_LEVELS+1];
           inodePath[0] = Block.rootInode;
           directoryBlockPath = new DirectoryBlock[MAX_LEVELS+1];
           directoryBlockPath[0] = Block.rootDirectoryBlock; 
           BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
           while (true)
           {
               printWorkingDirectory();
               System.out.print("> ");  
               commandLine = br.readLine();           
               processCommand(commandLine);               
          }
        }
        catch(IOException e)
        {
           System.out.println(e);
           System.exit(1);
        }             
      }
      
      public static void help()
      {
         System.out.println("Command                    Description");
         System.out.println("======================     ==============================================");
         System.out.println("help                       Displays commands and their description");
         System.out.println("pwd                        Print working directory");
         System.out.println("touch filename             Create empty file filename in the current working directory");
         System.out.println("cp filename1 filename2     Copy filename1 in the real current working directory to filename2");
         System.out.println("more filename              Display contents of file filename");
         System.out.println("rm filename                Delete file filename"); 
         System.out.println("stat(-L) filename          Shows number of blocks and number of links for file filename or source file if symbolic link");
         System.out.println("mkdir dirname              Create firectory dirname");
         System.out.println("ls                         List files in the current working directory");
         System.out.println("ln(-s) filename1 filename2 create hard/soft link filename2 for filename1");
         System.out.println("cd dirname                 Changes to subdirectory dirname");
         System.out.println("up                         Changes to parent directory");
         System.out.println("exit                       Terminates the program");           
         
      }
      
      public static void processCommand(String line)
      {
         StringTokenizer st = new StringTokenizer(line);
         String [] parameters = new String[st.countTokens()-1];
         String commandName = st.nextToken();
         int count = 0;
         while (st.hasMoreElements()) 
         {
            parameters[count++] = st.nextToken();                
         }
         
  
         if (commandName.equals("help"))
            help();       
         else if (commandName.equals("pwd"))
            printWorkingDirectory();
         else if (commandName.equals("touch"))
         {
            if (count == 1)
                createFile(parameters[0]);
            else System.out.println("Usage: touch filename");     
         }   
         else if (commandName.equals("cp"))
         {
            if (count == 2)
               copy(parameters[0],parameters[1]);
            else System.out.println("Usage: cp filename1 filename2");      
         } 
         else if (commandName.equals("more"))
         {
           if (count == 1)
              showFile(parameters[0]);
           else System.out.println("Usage: more filename");   
         }
         else if (commandName.equals("rm"))
        {
           if (count == 1)
              deleteFile(parameters[0]);
           else System.out.println("Usage: rm filename");   
         }
         else if (commandName.equals("stat"))
         {
           if (count == 1 && !parameters[0].equals("-L")){
              showInfo(parameters[0]);
            }
            else if(count == 2 && parameters[0].equals("-L")){
                showInfoS(parameters[1]);
            }
           else System.out.println("Usage: stat filename");   
         } 
         else if (commandName.equals("mkdir"))
         {
           if (count == 1)
              createDirectory(parameters[0]);
           else System.out.println("Usage: mkdir dirname");   
         }
         else if (commandName.equals("ls"))
         {
           if (count == 0)
              listDirectory();
           else System.out.println("Usage: ls");   
         }
         else if (commandName.equals("cd"))
         {
           if (count == 1)
              changeWorkingDirectory(parameters[0]);
           else System.out.println("Usage: cd dirname");   
         }                                 
         else if (commandName.equals("up"))
         {
           if (count == 0)
              changeWorkingDirectoryToParent();
           else System.out.println("Usage: up");              
         }                                      
         else if (commandName.equals("exit"))
         {
           if (count == 0)
              System.exit(1);
           else System.out.println("Usage: exit");   
         }
         
         else if(commandName.equals("ln")){
             if(count == 2 && !parameters[0].equals("-s")){
                 makeHardLink(parameters[0], parameters[1]);
                }
             else if (count == 3 && parameters[0].equals("-s")){
                 makeSoftLink(parameters[1], parameters[2]);
                }
             else{
                 System.out.println("Usage: ln");
                }
            }
        
        
         System.out.println();
      }
      
      public static void makeHardLink(String sourceFileName, String hardLinkName){
         int i = currentWorkingDirectoryDirNode.getInode(sourceFileName);
         int j = currentWorkingDirectoryDirNode.getInode(hardLinkName);
          if (i == -1 || j != -1)
         {
            System.out.println("Cannot create hard link!");
            return;
         }         
         boolean linked = currentWorkingDirectoryDirNode.addEntry(hardLinkName, i, "HardLink");
         if(linked == true){
             Inode inode = (Inode)Block.getBlock(i);
             //System.out.println(i);
             inode.setLinkCount(inode.getLinkCount() + 1);
             //System.out.println(currentWorkingDirectoryDirNode.getInode("p2"));
            }
         else{
             System.out.println("Failed to add hard link entry!");
            }
        }
      public static void makeSoftLink(String sourceFileName, String SoftLinkName){
          int i = currentWorkingDirectoryDirNode.getInode(sourceFileName);
          int j = currentWorkingDirectoryDirNode.getInode(SoftLinkName);
          if (i == -1 || j != -1)
         {
            System.out.println("Cannot create soft link!");
            return;
         }
         int k = Block.newInode();
         byte[] data = (sourceFileName).getBytes();
         if (k != -1)
         {
            currentWorkingDirectoryDirNode.addEntry(SoftLinkName, k, "SymLink");  
            Inode inode = (Inode)Block.getBlock(k);
            int count = 0;
            while (count < data.length)
                {   
                   int j1 = Block.newDataBlock();
                   if (j1 == -1)
                   {
                      System.out.println("Cannot complete copying: No available data block");
                      break;
                   }
                   if (!inode.addBlock(j1))
                   { 
                      System.out.println("Max file size exceeded!");
                      return;
                   }
                   DataBlock datablock = (DataBlock) Block.getBlock(j1);   
                   int num = datablock.addBytes(data,count);
                   count += num;
                }
         }
         else {
            System.out.println("Cannot create file: Maximum file limit reached!");
         }
        }
      public static void printWorkingDirectory()
      {
         for(int i=0; i<numLevels; i++)         
            System.out.print("/"+currentWorkingDirectory[i]);            
      }
      
      public static void createFile(String filename)
      {
         if (currentWorkingDirectoryDirNode.getInode(filename) != -1)
         {
            System.out.println("File exists!");
            return;
         }
         int i = Block.newInode();
         if (i != -1)
         {
            currentWorkingDirectoryDirNode.addEntry(filename,i, "Regular file");  
            Inode inode = (Inode)Block.getBlock(i);
            int j = Block.newDataBlock();
            if (j != -1)
               inode.addBlock(j);
            else System.out.println("Cannot create file: no available data block");
         }
         else {
            System.out.println("Cannot create file: Maximum file limit reached!");
         }
      }      
      public static void copy(String file1, String file2)
      {
         try {
           BufferedReader file = new BufferedReader(new FileReader(file1));
           int i = -1;
           int noOfLinks = 1;
           String type = "Regular file";
           if(currentWorkingDirectoryDirNode.getType(file2).equals("SymLink")){
               int s = currentWorkingDirectoryDirNode.getInode(file2);
               Inode inode = (Inode)Block.getBlock(s);
               int[] blocks = inode.getBlockAddresses();
               byte[] b1 = null;
               for(int count = 0; count<blocks.length; count++)
            {
              DataBlock datablock1 = (DataBlock)Block.getBlock(blocks[count]);
              b1 = datablock1.getBytes();
               
            }
            copy(file1, new String(b1));
            return;
            }
           if (currentWorkingDirectoryDirNode.getInode(file2) != -1){
              i = currentWorkingDirectoryDirNode.getInode(file2);
              type = currentWorkingDirectoryDirNode.getType(file2);
              noOfLinks = ((Inode)Block.getBlock(i)).getLinkCount();
              int success = deleteForCopy(file2);
              if(success == -1){
                  return;
                }
            }
           else{
              i = Block.newInode();
            }
           
           if (i != -1) {
             currentWorkingDirectoryDirNode.addEntry(file2,i, type);
                String line = null;
                byte[] data = null;
                while ( (line = file.readLine()) != null)
                {
                   byte[] b = (line+"\n").getBytes();
                   if (data == null)
                      data = b;
                   else {
                      int index;
                      byte[] temp = new byte[data.length+b.length];
                      for(index=0; index<data.length;index++)
                         temp[index] = data[index];
                      for(int k=0; k<b.length; k++)
                         temp[index+k] = b[k];
                      data = temp;       
                   }   
                }
                Inode inode = (Inode)Block.getBlock(i);
                inode.setNotFree();
                inode.setLinkCount(noOfLinks);
                int count = 0;
                while (count < data.length)
                {   
                   int j = Block.newDataBlock();
                   if (j == -1)
                   {
                      System.out.println("Cannot complete copying: No available data block");
                      break;
                   }
                   if (!inode.addBlock(j))
                   { 
                      System.out.println("Max file size exceeded!");
                      return;
                   }
                   DataBlock datablock = (DataBlock) Block.getBlock(j);   
                   int num = datablock.addBytes(data,count);
                   count += num;
                }
             }   
           else {
              System.out.println("Cannot create file: Maximum file limit reached!");
           }   
         }
         catch(FileNotFoundException e)
         {
            System.out.println(file1+" could not found!");
         }      
         catch(IOException e)
         {
            System.out.println(e);
         }    
      }
      
      public static void showFile(String file) 
      {
         BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
         int i = currentWorkingDirectoryDirNode.getInode(file);
         if (i != -1)
         {
            Inode inode = (Inode)Block.getBlock(i);
            if (inode.isDirectory())
            {
               System.out.println(file+" is a directory!");
               return;
            }
            int[] blocks = inode.getBlockAddresses();
            for(int count = 0; count<blocks.length; count++)
            {
              DataBlock datablock = (DataBlock)Block.getBlock(blocks[count]);
               byte[] b = datablock.getBytes();
               if (b != null)
               {
                  if(currentWorkingDirectoryDirNode.getType(file).equals("SymLink")){                     
                     showFile(new String(b));
                      
                    }
                    else{
                   System.out.print(new String(b));
                }
                  //System.out.println("--More--(Press Enter to continue)");
                  //standardInput.readLine();
               }   
            }
         }
         else System.out.println("No such file");
      }
      
      public static int deleteForCopy(String file)
      {
        int i = currentWorkingDirectoryDirNode.getInode(file);
        if (i != -1)
        {
            Inode inode = (Inode)Block.getBlock(i);
            if (inode.isDirectory())
            {
               System.out.println(file+" is a directory!");
               return -1;
            }   
            int[] blocks = inode.getBlockAddresses();
            for(int j=0; j<blocks.length; j++)
                  ((DataBlock)Block.getBlock(blocks[j])).setFree();
            int d = inode.getSingleIndirectBlockNo();   
            if (d != -1)
            {
               AddressBlock ab = ((AddressBlock)Block.getBlock(d));
               int[] addresses = ab.getAddresses();
               for(int j=0; j<addresses.length; j++)
                  ((DataBlock)Block.getBlock(addresses[j])).setFree();  
               ab.setFree();            
            }   
            inode.setFree();
            currentWorkingDirectoryDirNode.removeEntry(file,i);
            return 1;   
        }
        else{ System.out.println("No such file!");
            return -1;
        }
      }
      
      public static void deleteFile(String file)
      {
        int i = currentWorkingDirectoryDirNode.getInode(file);
        if (i != -1)
        {
            Inode inode = (Inode)Block.getBlock(i);
            if (inode.isDirectory())
            {
               System.out.println(file+" is a directory!");
               return;
            }   
            if (inode.getLinkCount() <= 1){
            int[] blocks = inode.getBlockAddresses();
            for(int j=0; j<blocks.length; j++)
                  ((DataBlock)Block.getBlock(blocks[j])).setFree();
            int d = inode.getSingleIndirectBlockNo();   
            if (d != -1)
            {
               AddressBlock ab = ((AddressBlock)Block.getBlock(d));
               int[] addresses = ab.getAddresses();
               for(int j=0; j<addresses.length; j++)
                  ((DataBlock)Block.getBlock(addresses[j])).setFree();  
               ab.setFree();            
            }   
            inode.setFree();
            currentWorkingDirectoryDirNode.removeEntry(file,i);
           }
           
           else{
               inode.setLinkCount(inode.getLinkCount() -1);
               currentWorkingDirectoryDirNode.removeEntry(file,i);
            }
        }
        else System.out.println("No such file!");
      }
      
      public static void showInfo(String file)
      {
        int i = currentWorkingDirectoryDirNode.getInode(file);
        
        if (i != -1)
        {
           Inode inode = (Inode)Block.getBlock(i);
           //if (inode.isDirectory())
              //System.out.print("Directory ");
           //else System.out.print("Regular file "); 
           System.out.print(currentWorkingDirectoryDirNode.getType(file) + " ");
           System.out.print("InodeNo: "+inode.getInodeNode() + "    # of data blocks: " + inode.getSize() + "    # of links: " + inode.getLinkCount());      
        }
        else System.out.println("No such file");                  
      }     
      
      public static void showInfoS(String file)
      {
        int i = currentWorkingDirectoryDirNode.getInode(file);
        if(currentWorkingDirectoryDirNode.getType(file).equals("SymLink")){
               int s = currentWorkingDirectoryDirNode.getInode(file);
               Inode inode = (Inode)Block.getBlock(s);
               int[] blocks = inode.getBlockAddresses();
               byte[] b1 = null;
               for(int count = 0; count<blocks.length; count++)
            {
              DataBlock datablock1 = (DataBlock)Block.getBlock(blocks[count]);
              b1 = datablock1.getBytes();
               
            }
            showInfo(new String(b1));
            return;
            }
        if (i != -1)
        {
           Inode inode = (Inode)Block.getBlock(i);
           //if (inode.isDirectory())
              //System.out.print("Directory ");
           //else System.out.print("Regular file "); 
           System.out.print(currentWorkingDirectoryDirNode.getType(file) + " ");
           System.out.print("InodeNo: "+inode.getInodeNode() + "    # of data blocks: " + inode.getSize() + "    # of links: " + inode.getLinkCount());      
        }
        else System.out.println("No such file");                  
      }     
      
      public static void createDirectory(String file)
      {
         int i = currentWorkingDirectoryDirNode.getInode(file);
         if (i != -1)
             System.out.println("File exists!");
         else {
            i = Block.newInode();
            if (i == -1)
               System.out.println("Maximum file number reached!");
            else {
                currentWorkingDirectoryDirNode.addEntry(file,i, "Directory");
                Inode inode = (Inode)Block.getBlock(i);
                inode.setDirectory();
                int j = Block.newDirectoryBlock();
                if (j == -1)
                   System.out.println("Maximum file number reached!");
                else {
                   inode.addBlock(j);                   
                }   
            } 
         }    
      }
      
      public static void listDirectory() 
      {
         String[] names = currentWorkingDirectoryDirNode.getNames();
         for(int i=0; i<names.length; i++)
            System.out.println(names[i]);
      }
      
      public static void changeWorkingDirectory(String file)
      {
         int i = currentWorkingDirectoryDirNode.getInode(file);
         if(currentWorkingDirectoryDirNode.getType(file).equals("SymLink")){
               int s = currentWorkingDirectoryDirNode.getInode(file);
               Inode inode = (Inode)Block.getBlock(s);
               int[] blocks = inode.getBlockAddresses();
               byte[] b1 = null;
               for(int count = 0; count<blocks.length; count++)
            {
              DataBlock datablock1 = (DataBlock)Block.getBlock(blocks[count]);
              b1 = datablock1.getBytes();
               
            }
            changeWorkingDirectory(new String(b1));
            return;
            }
         if (i == -1)
            System.out.println("No such file!");
         else {
            Inode inode = (Inode)Block.getBlock(i);
            if (!inode.isDirectory())
               System.out.println("Not a directory!");
            else {
                currentWorkingDirectoryInode = inode;
                int[] b = inode.getBlockAddresses();
                currentWorkingDirectoryDirNode = (DirectoryBlock)Block.getBlock(b[0]); 
                currentWorkingDirectory[numLevels++] = file;
                inodePath[numLevels] = currentWorkingDirectoryInode;
                directoryBlockPath[numLevels] = currentWorkingDirectoryDirNode;
            }   
         }   
      }
      
      public static void changeWorkingDirectoryToParent()
      {
         if (numLevels > 0)
         {
             numLevels--;
             currentWorkingDirectoryInode = inodePath[numLevels];
             currentWorkingDirectoryDirNode = directoryBlockPath[numLevels];              
         }
      }
         
}
