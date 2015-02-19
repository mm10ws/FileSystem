
class AddressBlock extends Block {
   protected int[] addresses;
   protected int numAddresses;
   protected static final int NUM_ADDRESSES = 32;
   
   public AddressBlock(int bNo)
   {
      super(bNo);
      addresses= new int[NUM_ADDRESSES];
      numAddresses = 0;
   }

   public AddressBlock(int bNo, boolean isFree)
   {
      super(bNo,isFree);
      addresses= new int[NUM_ADDRESSES];
      numAddresses = 0;
   }
   
   public boolean addAddress(int address)
   {
      if (numAddresses < NUM_ADDRESSES)
      {
         addresses[numAddresses++] = address;    
         return true;
      }
      return false;     
   }
   
   public int[] getAddresses()
   {
      if (numAddresses == 0)
        return null;
      int[] result = new int[numAddresses];
      for(int i=0; i<result.length; i++)
         result[i] = addresses[i];
      return result;   
   }
   
   public int getNumAddresses()
   {
     return numAddresses;
   }
   
   public boolean isFull()
   {
      return (numAddresses == NUM_ADDRESSES);
   }

   public void setFree()
   {
      numAddresses = 0;
      isFree = true;
   }   
}
