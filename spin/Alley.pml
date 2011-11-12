int alley_sem = 1;
int alley_atomic = 1;
int alley_currentDir = 0;
int alley_count = 0;

#define FREE 0
#define A 1
#define B 2


inline sem_P( sem ){
	atomic { sem > 0 -> sem = sem - 1 }	
}

inline sem_V( sem ){
	atomic { sem = sem + 1 }
	
}

inline alley_enter( dir ) {
	sem_P(alley_atomic);
	if
	:: (alley_currentDir!=dir) ->
	   sem_V( alley_atomic );
	   sem_P( alley_sem );
	   sem_P( alley_atomic)
	:: (alley_currentDir==dir) -> skip
	fi;
	alley_currentDir = dir;
	alley_count = alley_count +1;
	sem_V( alley_atomic )
}
 

inline alley_leave( dir ) {
	sem_P( alley_atomic );
	alley_count = alley_count - 1;
	if
	:: (alley_count == 0) -> 
	    alley_currentDir = FREE;
	    sem_V( alley_sem )
	:: (alley_count != 0)  -> skip
	fi;
	sem_V( alley_atomic )
}


active [4] proctype car1()
{

do
:: alley_enter( A );
   alley_leave( A )
od

}


active [4] proctype  car2()
{
do
:: alley_enter( B );
   alley_leave( B )
od
}


active proctype check_alley_count() {
	(alley_count > 5) -> assert(false)
}