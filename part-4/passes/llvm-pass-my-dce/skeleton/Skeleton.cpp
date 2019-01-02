#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include "llvm/IR/InstIterator.h"
#include "llvm/ADT/SmallVector.h"

using namespace llvm;

static bool removeDCE(Function &F){
    bool dceFound = false;
    SmallVector<Instruction *, 128> WL;

    for(Function::iterator bb = F.begin(), e = F.end(); bb!=e;++bb){
        for(BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++ i){
            WL.push_back(&*i);
        }
    }

    while (!WL.empty()){
        Instruction* I = WL.pop_back_val();
        if(isInstructionTriviallyDead(I)){
            dceFound = true;
            I->eraseFromParent();
        }
    }

    return dceFound;
}



namespace {
  struct MyDCE : public FunctionPass {
    static char ID;
      MyDCE() : FunctionPass(ID) {}

    virtual bool runOnFunction(Function &F) {

        while(removeDCE(F));

      return true;
    }
  };
}

char MyDCE::ID = 0;

__attribute__((unused)) static RegisterPass<MyDCE>
        X("live", "My dead code elimination"); // NOLINT
