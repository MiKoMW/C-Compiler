#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include "llvm/IR/InstIterator.h"
#include "llvm/ADT/SmallVector.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/InstrTypes.h"

#include <set>
#include <map>
#include <vector>

using namespace llvm;

typedef std::set<Instruction*>* InstSet;
typedef std::set<Value*>* ValSet;
typedef std::map<BasicBlock*, ValSet> BBMap;
typedef std::map<Instruction*, ValSet> InstMap;

namespace {

  struct MyDCE : public FunctionPass {
    static char ID;
    MyDCE() : FunctionPass(ID) {}

    virtual bool runOnFunction(Function &F) {

        // Print live set.
        InstSet deadcode = findDeadCode(F, true);

        while(deadcode->size() > 0){
            for(std::set<Instruction*>::iterator it = deadcode->begin(); it != deadcode->end(); it++) {
                (*it)->eraseFromParent();
            }
            deadcode = findDeadCode(F, false);
        }

        return true;
    }

    // Return a set of Dead Instruction. set print_liveness to be true to print the live set.
      virtual InstSet findDeadCode(Function &F, bool print_liveness){

          std::set<BasicBlock*> bb_WL;
          BBMap bb_liveIn;
          BBMap bb_liveOut;
          InstMap inst_liveIn;
          InstMap inst_liveOut;
          Function::iterator funIter;
          BasicBlock::iterator bbIter;
          std::set<Value*>::iterator ValIt;
          std::set<Instruction*>::iterator InstIt;

          for (Function::iterator bb = F.begin( ), e = F.end(); bb != e; ++bb) {

              BasicBlock *basicBlock = (&*bb);
              bb_WL.insert(basicBlock);
              ValSet liveIn = new std::set<Value*>();
              bb_liveIn.insert(std::pair<BasicBlock*, ValSet >(basicBlock, liveIn));

              ValSet liveOut = new std::set<Value*>();
              bb_liveOut.insert(std::pair<BasicBlock*, ValSet>(basicBlock, liveOut));

          }

          // Solving Data flow Equation between basic blocks.
          while (!( bb_WL.empty() ))
          {
              BasicBlock *bb = *( bb_WL.begin() );
              bb_WL.erase(bb);

              // Calculate gen set and kill set for each block.
              ValSet gen = genSet_NoPhi(bb);
              ValSet kill = killSet(bb);

              // Debug ----------------------------------------------------------------

              /*
              errs() << *bb << "\n";

              errs() << "Gen size : " << gen->size() << "Kill Size : " << kill->size() << "\n";

              for (ValIt = gen->begin(); ValIt != gen->end(); ValIt++) {
                  errs() << "Gen \n";
                  (*ValIt)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              for (ValIt = kill->begin(); ValIt != kill->end(); ValIt++) {
                  errs() << "Kill \n";

                  (*ValIt)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              */


              // Calculate the In and Out set of the basic block.
              ValSet liveOut = getLiveOut(bb, bb_liveIn);
              delete bb_liveOut[bb];
              bb_liveOut[bb] = liveOut;

              ValSet temp_liveIn = bb_liveIn[bb];
              // In = Gen U (Out - Kill)
              ValSet liveIn = updateInSet(liveOut, gen, kill);

              // Debug ---------------------------------------------------------
              /*
              errs() << *bb << "\n";
              errs() << "liveIn size : " << liveIn->size() << "liveOut Size : " << liveOut->size() << "\n";
              for (ValIt = liveIn->begin(); ValIt != liveIn->end(); ValIt++) {
                  errs() << "liveIn \n";
                  (*ValIt)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              for (ValIt = liveOut->begin(); ValIt != liveOut->end(); ValIt++) {
                  errs() << "liveOut \n";

                  (*ValIt)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              */

              // Work List Algorithm.
              if (!compareTwoInstSets(temp_liveIn,liveIn))
              {
                  delete temp_liveIn;
                  bb_liveIn[bb] = liveIn;
                  for (pred_iterator pit = pred_begin(bb); pit != pred_end(bb); pit++) {
                      bb_WL.insert(*pit);
                  }
              }
              else
              {
                  delete liveIn;
              }

              delete gen;
              delete kill;
          }

          // Calculate live set per Instruction inside basic block.
          for (funIter = F.begin(); funIter != F.end(); funIter++)
          {
              BasicBlock *bb = &*funIter;
              bbIter = bb->end();
              bool lastInst = true;
              Instruction *cur_inst, *next_inst;

              do {
                  bbIter--;
                  next_inst = cur_inst;
                  cur_inst = &*bbIter;

                  inst_liveOut[cur_inst] = new std::set<Value*>();
                  if (lastInst) {
                      lastInst = false;
                      *(inst_liveOut[cur_inst]) = *(bb_liveOut[bb]);
                  }
                  else {
                      *(inst_liveOut[cur_inst]) = *(inst_liveIn[next_inst]);
                  }

                  inst_liveIn[cur_inst] = getInstIn(cur_inst, inst_liveOut);
              } while (bbIter != bb->begin());

              // Debug----------------------------------------------------
              //errs() << compareTwoInstSets(inst_liveIn[cur_inst],bb_liveIn[bb]);
          }

          InstSet deadCode = new std::set<Instruction*>();
          for (funIter = F.begin(); funIter != F.end(); funIter++)
          {
              BasicBlock *bb = &*funIter;
              for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                  Instruction *inst = &*bbIter;
                  if (canBeRemoved(inst)) {
                      std::set<Value*>::iterator findInst = inst_liveOut[inst]->find(inst);
                      if (findInst == inst_liveOut[inst]->end()) {
                          deadCode->insert(inst);
                      }
                  }
              }
          }

        //Debug ----------------------------------------

        /*
        errs() << "Print Liveness \n";
        for (funIter = F.begin(); funIter != F.end(); funIter++)
        {
            BasicBlock *bb = &*funIter;

            //printInstSet(bb_liveIn[bb]);

            errs() << *bb << "\n";
            //printInstSet(bb_liveOut[bb]);

            errs() << "genset:\n";
            printInstSet(genSet(bb));


            errs() << "genset Phi:\n";
            printInstSet(genSet_Phi(bb));

            errs() << "genset NoPhi:\n";
            printInstSet(genSet_NoPhi(bb));

            errs() << "killset:\n";
            printInstSet(killSet(bb));


            for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                Instruction *inst = &*bbIter;

                printInstSet(inst_liveIn[inst]);
                errs() << *inst << "\n";
                printInstSet(inst_liveOut[inst]);

            }

            errs() << "\n";
        }
        errs() << "====================\n";

    */


          // Print live set for each instruction need to be print.
          if(print_liveness){



              for (funIter = F.begin(); funIter != F.end(); funIter++)
              {
                  BasicBlock *bb = &*funIter;
                  Instruction *inst;
                  bool isFirst = true;
                  for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                      inst = &*bbIter;

                      if(isa<PHINode>(inst)){

                      } else{

                          if(isFirst && inst->isTerminator() && !isa<ReturnInst>(inst)){}
                          else{
                          printInstSet(inst_liveIn[inst]);}

                      }
                      isFirst = false;
                  }
                  if(inst->isTerminator() && !isa<ReturnInst>(inst)){}
                  else {
                      printInstSet(inst_liveOut[inst]);
                  }
              }
          }

          return deadCode;

      }


      virtual void printInstSet(ValSet instSet){

        int size = instSet->size();

        errs() << "{";
        if(size == 0){
            errs() << "}\n";
            return;
        }

        int con = 0;
        std::set<Value*>::iterator it;
        for (it = instSet->begin(); it != instSet->end(); it++) {
            (*it)->printAsOperand(errs(),false);
            if(++con < size){
                errs()<<",";
            }
        }

        errs() << "}\n";
        return;
    }


     virtual ValSet getInstIn(Instruction *inst, std::map<Instruction *, ValSet> &liveAfter) {
         ValSet liveBefore = new std::set<Value*>();
          *liveBefore = *(liveAfter[inst]);
          if(!inst->isTerminator()){
              liveBefore->erase(inst);
          }
          User::op_iterator opit;
          //errs() << "Instruction : " << *inst << "\n\n";
          for (opit = inst->op_begin(); opit != inst->op_end(); opit++)
          {
              Use *use = &*opit;
              Value *used_val = use->get();
              Instruction *used_inst = dyn_cast<Instruction>(used_val);
              if (used_inst != nullptr) {
                  //errs() << *used_inst << "\n";
                  liveBefore->insert(used_inst);
              }

              if(isa<Argument>(used_val)){
                  liveBefore->insert(used_val);
              }

          }

          return liveBefore;
      }

      virtual bool canBeRemoved(Instruction *inst) {
        unsigned opcode = inst->getOpcode();

        if(inst->mayHaveSideEffects() || inst->isTerminator()){
            return false;
        }

          return ( inst->isBinaryOp()
                   || inst->isCast()
                   || opcode == 30
                   || opcode == 31
                   || opcode == 33
                   || opcode == 56
                   || opcode == 60
                   || opcode == 61
                   || opcode == 63
                   || opcode == 64
                   || opcode == 52
                   || opcode == 53
          );

    }


      virtual bool compareTwoInstSets(ValSet A, ValSet B) {
          if(A->size() != B->size()){
              return false;
          }
          else{
              for(auto &value : *A){
                  if(B->find(value) == B->end()){
                      return false;
                  }
              }
          }
          return true;
      }

      virtual ValSet genSet(BasicBlock* bb) {
          ValSet gen = new std::set<Value*>();
          BasicBlock::iterator bbit;

          ValSet defined = new std::set<Value*>();

          for (bbit = bb->begin(); bbit != bb->end(); bbit++)
          {
              Instruction *inst = &*bbit;
              User::op_iterator opit;


              for (opit = inst->op_begin(); opit != inst->op_end(); opit++)
              {
                  Use *use = &*opit;
                  Value *used_val = use->get();

                  if(isa<Argument>(used_val)){
                      if (defined->find(used_val) == defined->end() ){
                          gen->insert(used_val);
                      }
                  }


                  Instruction *used_inst = dyn_cast<Instruction>(used_val);
                  if (used_inst != nullptr) {
                      if (defined->find(used_inst) == defined->end() ){
                          gen->insert(used_inst);
                      }
                  }



              }
              defined->insert(inst);
          }
          delete defined;
          return gen;
      }

      virtual ValSet genSet_Phi(BasicBlock* bb) {
          ValSet gen = new std::set<Value*>();
          BasicBlock::iterator bbit;

          ValSet defined = new std::set<Value*>();

          for (bbit = bb->begin(); bbit != bb->end(); bbit++)
          {
              Instruction *inst = &*bbit;
              if(isa<PHINode>(inst)){

                  PHINode * thisPhi = dyn_cast<PHINode>(inst);

                  errs() <<"This Phi is : "  << *thisPhi << "\n";
                  User::op_iterator opit;
                  for (opit = inst->op_begin(); opit != inst->op_end(); opit++)
                  {
                      Use *use = &*opit;
                      Value *used_val = use->get();

                      if(isa<Argument>(used_val)){
                          if (defined->find(used_val) == defined->end() ){
                              gen->insert(used_val);
                          }
                      }


                      Instruction *used_inst = dyn_cast<Instruction>(used_val);
                      if (used_inst != nullptr) {
                          if (defined->find(used_inst) == defined->end() ){
                              gen->insert(used_inst);
                          }
                      }
                  }
              }
              defined->insert(inst);
          }
          delete defined;
          return gen;
      }

      virtual ValSet genSet_NoPhi(BasicBlock* bb) {
          ValSet gen = new std::set<Value*>();
          BasicBlock::iterator bbit;

          ValSet defined = new std::set<Value*>();

          for (bbit = bb->begin(); bbit != bb->end(); bbit++)
          {
              Instruction *inst = &*bbit;
              if(!isa<PHINode>(inst)){
                  User::op_iterator opit;
                  for (opit = inst->op_begin(); opit != inst->op_end(); opit++)
                  {
                      Use *use = &*opit;
                      Value *used_val = use->get();

                      if(isa<Argument>(used_val)){
                          if (defined->find(used_val) == defined->end() ){
                              gen->insert(used_val);
                          }
                      }


                      Instruction *used_inst = dyn_cast<Instruction>(used_val);
                      if (used_inst != nullptr) {
                          if (defined->find(used_inst) == defined->end() ){
                              gen->insert(used_inst);
                          }
                      }
                  }
              }
              defined->insert(inst);
          }
          delete defined;
          return gen;
      }



      virtual ValSet killSet(BasicBlock* bb) {
          ValSet kill = new std::set<Value*>();
          for (BasicBlock::iterator bbit = bb->begin(); bbit != bb->end(); bbit++) {
              Instruction* inst = &*bbit;
              if(inst->isTerminator()){

              }/*else if(isa<CallInst>(inst)){
                  errs() << "Call Inst " << inst << "\n";
                  inst->printAsOperand(errs(),false);
              }*/
              else {
                  kill->insert(&*bbit);
              }
          }
          return kill;
      }

      virtual ValSet getLiveOut(BasicBlock *bb, std::map<BasicBlock *, ValSet> &inSet) {

          ValSet outSet = new std::set<Value*>();
          for (succ_iterator sit = succ_begin(bb); sit != succ_end(bb); sit++) {
              ValSet liveBefore_bb = inSet[*sit];
              for (std::set<Value*>::iterator it = liveBefore_bb->begin(); it != liveBefore_bb->end(); it++) {
                  outSet->insert(*it);
              }

              BasicBlock *basicBlock = *sit;
              for (BasicBlock::phi_iterator iter = basicBlock->phis().begin(); iter!=basicBlock->phis().end();iter++){
                  Instruction *inst = &*iter;
                  PHINode *thisPhi = dyn_cast<PHINode>(inst);

                  User::op_iterator opit;
                  for (opit = thisPhi->op_begin(); opit != thisPhi->op_end(); opit++)
                  {
                      Use *use = &*opit;
                      Value *used_val = use->get();
                      if(bb == (*thisPhi).getIncomingBlock(*use)){

                          if(isa<Argument>(used_val)){
                              outSet->insert(used_val);
                          }

                          Instruction *used_inst = dyn_cast<Instruction>(used_val);
                          if (used_inst != nullptr) {
                              outSet->insert(used_val);
                          }

                      }
                  }
              }
          }
          return outSet;
      }

      // In = Gen U (Out - Kill)
      virtual ValSet updateInSet(ValSet out, ValSet gen, ValSet kill) {
          ValSet result = new std::set<Value *>();
          std::set<Value*>::iterator it;
          for (it = out->begin(); it != out->end(); it++) {
              result->insert(*it);
          }
          for (it = kill->begin(); it != kill->end(); it++) {
              result->erase(*it);
          }
          for (it = gen->begin(); it != gen->end(); it++) {
              result->insert(*it);
          }
          return result;
      }

  };
}

char MyDCE::ID = 0;

__attribute__((unused)) static RegisterPass<MyDCE>
        X("live", "My dead code elimination"); // NOLINT