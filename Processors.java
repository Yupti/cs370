package procs;

public class Processors extends MPI_Proc
{
	public Processors (MPI_World world, int rank)
	{
		super(world, rank);
	}

	public int[] toArray(int i){
        int array[] = {i};
        return array;
    }
	
	public void exec(int argc, String argv[]) throws InterruptedException
    {
        MPI_Init(argc, argv);
        MPI_Status status = new MPI_Status();

        // int data1[] = new int[4];
        // int data2[] = new int[4];
        // non problematic error message appears
        int[] toRoot = new int[2]; // to hold rank and energy value to send to root
        int[] indicator = new int[1];
        int[] task = new int[1];
        int[] commBuffer = {-99};
        int[] empty = {0};
        boolean isRequesting = true;
        boolean isWorking = true;
        int rank = MPI_Comm_rank(MPI_COMM_WORLD);
        int size = MPI_Comm_size(MPI_COMM_WORLD);
        
        int requestTag = 111;
        int taskTag = 222;
        int completeTag = 333;
        
        int generator = 1;
        
        
        if (rank == 0) 
        {
        	System.out.println("This is root.");
        	while (isWorking)
        	{
        		MPI_Recv(toRoot, 2, MPI_INT, MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, status);
        		if (toRoot[0] == 0 && toRoot[1] == 0)
        		{
        			System.out.println("All tasks are completed.");
        			isWorking = false;
        		}
        		else
        			System.out.println("Worker " + toRoot[0] + " completed Task " + toRoot[1]);
        		// must be always receiving, needs to add values to array
        		// needs while loop with terminating condition, maybe pass 0 and 0 to toRoot and check condition
        	}
        }
        
        if (rank == 1)
        {
        	System.out.println("This is generator.");
        	for (int i = 1; i <= 30; i++) 
        	{
        		MPI_Recv(indicator, 1, MPI_INT, 2, requestTag, MPI_COMM_WORLD, status);
        		System.out.println("Generator sending task " + i + " to Comm " + indicator[0]);
        		MPI_Send(toArray(i), 1, MPI_INT, indicator[0], taskTag, MPI_COMM_WORLD);
        	}
        	MPI_Recv(indicator, 1, MPI_INT, 2, requestTag, MPI_COMM_WORLD, status);
        	MPI_Send(empty, 1, MPI_INT, 2, taskTag, MPI_COMM_WORLD);
        	// needs to have a recv, once recv then can use send to send to communicator
        }
        
        if (rank >= 2 && rank <= 4)
        // place while loop w/ isWorking, needs if statement for completeTag
        {
        	System.out.println("This is comm " + rank);
        	while (isWorking)
        	{
        		if (isRequesting)
        		{
        			if (rank == 2)
        			{
        				MPI_Send(toArray(rank), 1, MPI_INT, generator, requestTag, MPI_COMM_WORLD);
        				MPI_Recv(task, 1, MPI_INT, generator, taskTag, MPI_COMM_WORLD, status);
        				isRequesting = false;
        			}
        		
        			// sends request, receives task (sending from left to right)
        		
        			else
        			{
        				MPI_Send(toArray(rank), 1, MPI_INT, rank - 1, requestTag, MPI_COMM_WORLD);
        				MPI_Recv(task, 1, MPI_INT, MPI_ANY_SOURCE, taskTag, MPI_COMM_WORLD, status);
        				isRequesting = false;
        			}
        		}
        	// not requesting, should be listening to receive from either worker or comm to send
        		else
        		{
        			MPI_Recv(commBuffer, 1, MPI_INT, MPI_ANY_SOURCE, requestTag, MPI_COMM_WORLD, status);
        			MPI_Send(task, 1, MPI_INT, commBuffer[0], taskTag, MPI_COMM_WORLD);
        			if (task[0] == 30) // indicating this is the final task that is being sent out
        			{
        				isRequesting = false;
        				isWorking = false;
        			}
        			else
        			{
        				isRequesting = true;
        			}
        		}
        	}
        	// if rank 2, looks to recv from 1 first, then checks whether worker 5 or comm 3 is requesting and sends, while true loop until receives task 30
        }
        
        if (rank >= 5) 
        {
        	System.out.println("This is worker " + rank);
        	Thread.sleep(2000);
        	// comm 2 -> worker 5, comm 3 -> worker 6, comm 4 -> worker 7 + 8
        }
    }
}