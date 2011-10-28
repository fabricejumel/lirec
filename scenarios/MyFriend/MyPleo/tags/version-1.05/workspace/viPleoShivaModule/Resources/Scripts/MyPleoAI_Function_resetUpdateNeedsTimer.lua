--------------------------------------------------------------------------------
--  Function......... : resetUpdateNeedsTimer
--  Author........... : Paulo F. Gomes
--  Description...... : Resets the need updater. Called every
--                      nNeedsTimerInterval seconds.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.resetUpdateNeedsTimer ( )
--------------------------------------------------------------------------------
	
	this.postEvent(this.nNeedsTimerInterval ( ),"onUpdateNeeds" )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------