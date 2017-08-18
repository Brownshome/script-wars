class Action:
    def __init__(self, code):
        self.code = code

Action.NOTHING = Action(0)
Action.MOVE = Action(1)
Action.SHOOT = Action(2)
