class Comm:
    is_symmetry_confirmed = False
    is_sym_eliminated = [False, False, False]  # 0 = rotational, 1 = vertical, 2 = horizontal

    # Constants for symmetry types
    SYM_ROTATIONAL = 0
    SYM_VERTICAL = 1
    SYM_HORIZONTAL = 2

    # Eliminate a specific symmetry if it has been confirmed invalid
    @staticmethod
    def eliminate_sym(sym):
        if not Comm.is_sym_eliminated[sym]:
            Comm.is_sym_eliminated[sym] = True
            print(f"Symmetry {sym} has been eliminated.")

    # Reset symmetry state (if needed, you can call this to start fresh)
    @staticmethod
    def reset_symmetry_state():
        Comm.is_symmetry_confirmed = False
        Comm.is_sym_eliminated = [False, False, False]
