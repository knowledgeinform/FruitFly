//=========================================================================
//	Queue Code
//=========================================================================

#define MALLOC_SIZE 4096
#define MALLOC_DEBUG
#define MACROSIZE 256			//use only 256 (100) to 2048(800) (current sector mask is FFF00)
#define MAXQUEUE 8
#use malloc.lib
#use error.c

typedef struct{
	int Data[MACROSIZE/2];
	}Item;

typedef struct node {
	Item item;
	struct node *next;
	}Node;

typedef struct queue{
	Node *front;
	Node *rear;
	int items;
	}Queue;


void InitializeQueue(Queue *);
int FullQueue ( Queue *);
int EmptyQueue ( Queue *);
int QueueItems ( Queue *);
int EnQueue(Item,Queue *);
int DeQueue(Item *, Queue *);
static void CopytoNode(Item, Node* );
static void CopytoItem(Node*, Item* );



//=========================================================================
//	Initialize Queue
//=========================================================================
void InitializeQueue(Queue *pq)
{
	pq->front=pq->rear=NULL;
	pq->items = 0;
}

//=========================================================================
//	Full Queue
//=========================================================================
int FullQueue (Queue *pq)
{
	return pq->items == MAXQUEUE;
}

//=========================================================================
//	Empty Queue
//=========================================================================
int EmptyQueue (Queue *pq)
{
	return pq->items == 0;
}

//=========================================================================
// Items in Queue
//=========================================================================
int QueueItems (Queue *pq)
{
	return pq->items;
}

//=========================================================================
//	EnQueue
//=========================================================================
int EnQueue( Item item, Queue *pq)
{
	Node *pnew;
	if (FullQueue(pq)) return 0;
	pnew = (Node *)malloc(sizeof(Node));
	if (pnew == NULL)
	{
		Error(QUEUEERROR);
		return 0;
	}
	CopytoNode(item,pnew);
	pnew->next=NULL;
	if (EmptyQueue(pq))
	{
		pq->front = pnew;
	}else{
	 	pq->rear->next=pnew;
	}
		pq->rear=pnew;
		pq->items++;
		return 1;
}

//=========================================================================
//	De Queue
//=========================================================================
int DeQueue(Item *pitem, Queue *pq)
{
	Node *pt;

	if (EmptyQueue(pq)) return 0;
	CopytoItem(pq->front,pitem);
	pt=pq->front;
	pq->front=pq->front->next;
	free(pt);
	pq->items--;
	if (pq->items == 0)
	{
		pq->rear = NULL;
	}
	return 1;
}

//=========================================================================
//	Copy to Node
//=========================================================================
void CopytoNode(Item item, Node *pn)
{
	pn->item=item;
}

//=========================================================================
//	Copy to Item
//=========================================================================
void CopytoItem(Node *pn, Item *pi)
{
	*pi = pn->item;
}