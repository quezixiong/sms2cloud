class InvaildCrentialError(Exception):
    msg = "invalid crential"


class CreateMenuError(Exception):
    msg = "Create menu falid"


class GetOpenIDError(Exception):
    pass

class GetQRFailed(Exception):
    pass
